// ─────────────────────────────────────────────────────────────
//  backend/routes/seats.js
//  GET /api/seats/:show_id   — returns full seat map for a show
//    Response: { rows, cols, booked: ['A1','B3',...] }
// ─────────────────────────────────────────────────────────────
const express = require('express');
const db      = require('../config/db');
const router  = express.Router();

// ── GET /api/seats/:show_id ──────────────────
router.get('/:show_id', async (req, res) => {
  try {
    const showId = req.params.show_id;

    // Get screen dimensions
    const [showRows] = await db.query(`
      SELECT sc.rows, sc.cols, sc.total_seats,
             sh.price_std, sh.price_prem
      FROM shows sh
      JOIN screens sc ON sc.id = sh.screen_id
      WHERE sh.id = ?`, [showId]);

    if (!showRows.length)
      return res.status(404).json({ error: 'Show not found.' });

    const screen = showRows[0];

    // Get all booked seat labels for this show
    const [bookedRows] = await db.query(`
      SELECT bs.seat_label, bs.seat_type
      FROM booking_seats bs
      JOIN bookings bk ON bk.id = bs.booking_id
      WHERE bs.show_id = ? AND bk.status != 'Cancelled'`,
      [showId]
    );

    const booked = bookedRows.map(r => r.seat_label);

    return res.json({
      rows:       screen.rows,
      cols:       screen.cols,
      total_seats: screen.total_seats,
      price_std:  screen.price_std,
      price_prem: screen.price_prem,
      booked      // array of seat labels already taken
    });
  } catch (err) {
    console.error('[GET /seats/:show_id]', err);
    return res.status(500).json({ error: 'Could not fetch seat map.' });
  }
});

module.exports = router;
