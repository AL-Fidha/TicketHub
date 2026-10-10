// ─────────────────────────────────────────────────────────────
//  backend/routes/bookings.js
//  POST   /api/bookings             — create booking
//  GET    /api/bookings/mine        — my bookings
//  GET    /api/bookings/:ref        — single booking by ref
//  PUT    /api/bookings/:ref/cancel — cancel booking
//  GET    /api/bookings             — admin: all bookings
// ─────────────────────────────────────────────────────────────
const express = require('express');
const db      = require('../config/db');
const { requireAuth, requireAdmin } = require('../middleware/auth');

const router = express.Router();

// ── booking ref generator ────────────────────
function genRef() {
  const ts   = Date.now().toString(36).toUpperCase();
  const rand = Math.random().toString(36).substring(2, 6).toUpperCase();
  return `TH-${ts}-${rand}`;
}

// ── POST /api/bookings ───────────────────────
router.post('/', requireAuth, async (req, res) => {
  const { show_id, seats } = req.body;
  // seats: [{ label: 'A1', type: 'premium' }, ...]

  if (!show_id || !seats || !seats.length)
    return res.status(400).json({ error: 'show_id and seats[] are required.' });

  if (seats.length > 8)
    return res.status(400).json({ error: 'Maximum 8 seats per booking.' });

  const conn = await db.getConnection();
  try {
    await conn.beginTransaction();

    // 1. Verify show exists and get prices
    const [[show]] = await conn.query(
      'SELECT * FROM shows WHERE id = ?', [show_id]);
    if (!show) { await conn.rollback(); return res.status(404).json({ error: 'Show not found.' }); }

    // 2. Check no seat is already booked (row-level lock)
    const labels = seats.map(s => s.label);
    const placeholders = labels.map(() => '?').join(',');
    const [conflict] = await conn.query(
      `SELECT bs.seat_label FROM booking_seats bs
       JOIN bookings bk ON bk.id = bs.booking_id
       WHERE bs.show_id = ? AND bk.status != 'Cancelled'
         AND bs.seat_label IN (${placeholders})
       FOR UPDATE`,
      [show_id, ...labels]
    );

    if (conflict.length) {
      await conn.rollback();
      const taken = conflict.map(r => r.seat_label).join(', ');
      return res.status(409).json({ error: `Seats already booked: ${taken}. Please choose different seats.` });
    }

    // 3. Calculate total
    const total = seats.reduce((sum, s) => {
      return sum + (s.type === 'premium'
        ? parseFloat(show.price_prem)
        : parseFloat(show.price_std));
    }, 0);

    // 4. Insert booking
    const ref = genRef();
    const [bRes] = await conn.query(
      'INSERT INTO bookings (booking_ref, user_id, show_id, total_amount, status) VALUES (?, ?, ?, ?, ?)',
      [ref, req.user.id, show_id, total, 'Confirmed']
    );
    const bookingId = bRes.insertId;

    // 5. Insert each seat
    for (const s of seats) {
      const price = s.type === 'premium' ? show.price_prem : show.price_std;
      await conn.query(
        'INSERT INTO booking_seats (booking_id, show_id, seat_label, seat_type, price) VALUES (?, ?, ?, ?, ?)',
        [bookingId, show_id, s.label, s.type || 'standard', price]
      );
    }

    await conn.commit();
    return res.status(201).json({ booking_ref: ref, booking_id: bookingId, total });

  } catch (err) {
    await conn.rollback();
    console.error('[POST /bookings]', err);
    return res.status(500).json({ error: 'Booking failed. Please try again.' });
  } finally {
    conn.release();
  }
});

// ── GET /api/bookings/mine ───────────────────
router.get('/mine', requireAuth, async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT bk.id, bk.booking_ref, bk.total_amount, bk.status, bk.booked_at,
             m.title AS movie, m.language, m.genre,
             m.poster_path,
             sh.show_date, sh.show_time,
             sc.name AS screen_name,
             t.name  AS theatre_name, t.city,
             GROUP_CONCAT(bs.seat_label ORDER BY bs.seat_label SEPARATOR ', ') AS seats,
             GROUP_CONCAT(bs.seat_type  ORDER BY bs.seat_label SEPARATOR ', ') AS seat_types
      FROM bookings bk
      JOIN shows sh ON sh.id = bk.show_id
      JOIN movies m  ON m.id  = sh.movie_id
      JOIN screens sc ON sc.id = sh.screen_id
      JOIN theatres t  ON t.id  = sc.theatre_id
      JOIN booking_seats bs ON bs.booking_id = bk.id
      WHERE bk.user_id = ?
      GROUP BY bk.id
      ORDER BY bk.booked_at DESC`,
      [req.user.id]
    );

    const TMDB_IMG = process.env.TMDB_IMAGE_BASE || 'https://image.tmdb.org/t/p/w500';
    return res.json(rows.map(r => ({
      ...r,
      poster_url: r.poster_path ? `${TMDB_IMG}${r.poster_path}` : null,
      seats: r.seats ? r.seats.split(', ') : []
    })));
  } catch (err) {
    console.error('[GET /bookings/mine]', err);
    return res.status(500).json({ error: 'Could not fetch bookings.' });
  }
});

// ── GET /api/bookings  (admin only) ──────────
router.get('/', requireAuth, requireAdmin, async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT bk.id, bk.booking_ref, bk.total_amount, bk.status, bk.booked_at,
             u.name AS customer_name, u.email AS customer_email,
             m.title AS movie, m.language,
             sh.show_date, sh.show_time,
             t.name AS theatre_name, t.city,
             GROUP_CONCAT(bs.seat_label ORDER BY bs.seat_label SEPARATOR ', ') AS seats
      FROM bookings bk
      JOIN users u    ON u.id   = bk.user_id
      JOIN shows sh   ON sh.id  = bk.show_id
      JOIN movies m   ON m.id   = sh.movie_id
      JOIN screens sc ON sc.id  = sh.screen_id
      JOIN theatres t ON t.id   = sc.theatre_id
      JOIN booking_seats bs ON bs.booking_id = bk.id
      GROUP BY bk.id
      ORDER BY bk.booked_at DESC`
    );
    return res.json(rows.map(r => ({ ...r, seats: r.seats ? r.seats.split(', ') : [] })));
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch all bookings.' });
  }
});

// ── GET /api/bookings/:ref ───────────────────
router.get('/:ref', requireAuth, async (req, res) => {
  try {
    const [rows] = await db.query(`
      SELECT bk.*, m.title AS movie, m.language, m.genre,
             m.poster_path,
             sh.show_date, sh.show_time, sh.price_std, sh.price_prem,
             sc.name AS screen_name,
             t.name AS theatre_name, t.location, t.city,
             u.name AS customer_name, u.email AS customer_email, u.phone,
             GROUP_CONCAT(bs.seat_label ORDER BY bs.seat_label SEPARATOR ', ') AS seats
      FROM bookings bk
      JOIN users u    ON u.id   = bk.user_id
      JOIN shows sh   ON sh.id  = bk.show_id
      JOIN movies m   ON m.id   = sh.movie_id
      JOIN screens sc ON sc.id  = sh.screen_id
      JOIN theatres t ON t.id   = sc.theatre_id
      JOIN booking_seats bs ON bs.booking_id = bk.id
      WHERE bk.booking_ref = ?
      GROUP BY bk.id`,
      [req.params.ref]
    );

    if (!rows.length) return res.status(404).json({ error: 'Booking not found.' });
    const b = rows[0];

    // Only owner or admin can view
    if (b.user_id !== req.user.id && req.user.role !== 'admin')
      return res.status(403).json({ error: 'Access denied.' });

    const TMDB_IMG = process.env.TMDB_IMAGE_BASE || 'https://image.tmdb.org/t/p/w500';
    return res.json({
      ...b,
      poster_url: b.poster_path ? `${TMDB_IMG}${b.poster_path}` : null,
      seats: b.seats ? b.seats.split(', ') : []
    });
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch booking.' });
  }
});

// ── PUT /api/bookings/:ref/cancel ────────────
router.put('/:ref/cancel', requireAuth, async (req, res) => {
  try {
    const [rows] = await db.query(
      'SELECT id, user_id, status FROM bookings WHERE booking_ref = ?',
      [req.params.ref]
    );
    if (!rows.length) return res.status(404).json({ error: 'Booking not found.' });

    const b = rows[0];
    if (b.user_id !== req.user.id && req.user.role !== 'admin')
      return res.status(403).json({ error: 'Access denied.' });

    if (b.status === 'Cancelled')
      return res.status(400).json({ error: 'Booking is already cancelled.' });

    await db.query("UPDATE bookings SET status = 'Cancelled' WHERE id = ?", [b.id]);
    return res.json({ message: 'Booking cancelled.' });
  } catch (err) {
    return res.status(500).json({ error: 'Could not cancel booking.' });
  }
});

module.exports = router;
