// ─────────────────────────────────────────────────────────────
//  backend/routes/admin.js
//  GET /api/admin/stats   — dashboard summary stats
//  GET /api/admin/users   — all users
// ─────────────────────────────────────────────────────────────
const express = require('express');
const db      = require('../config/db');
const { requireAuth, requireAdmin } = require('../middleware/auth');

const router = express.Router();

// ── GET /api/admin/stats ─────────────────────
router.get('/stats', requireAuth, requireAdmin, async (req, res) => {
  try {
    const [[{ total_movies }]]   = await db.query('SELECT COUNT(*) AS total_movies FROM movies');
    const [[{ total_theatres }]] = await db.query('SELECT COUNT(*) AS total_theatres FROM theatres');
    const [[{ total_users }]]    = await db.query("SELECT COUNT(*) AS total_users FROM users WHERE role = 'customer'");
    const [[{ total_bookings }]] = await db.query("SELECT COUNT(*) AS total_bookings FROM bookings WHERE status = 'Confirmed'");
    const [[{ total_revenue }]]  = await db.query("SELECT COALESCE(SUM(total_amount),0) AS total_revenue FROM bookings WHERE status = 'Confirmed'");

    return res.json({ total_movies, total_theatres, total_users, total_bookings, total_revenue });
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch stats.' });
  }
});

// ── GET /api/admin/users ─────────────────────
router.get('/users', requireAuth, requireAdmin, async (req, res) => {
  try {
    const [rows] = await db.query(
      'SELECT id, name, email, phone, role, created_at FROM users ORDER BY created_at DESC'
    );
    return res.json(rows);
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch users.' });
  }
});

module.exports = router;
