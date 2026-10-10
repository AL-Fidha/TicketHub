// ─────────────────────────────────────────────────────────────
//  backend/routes/theatres.js
//  GET  /api/theatres          — all theatres
//  GET  /api/theatres/:id      — single theatre with screens
// ─────────────────────────────────────────────────────────────
const express = require('express');
const db      = require('../config/db');
const router  = express.Router();

// ── GET /api/theatres ────────────────────────
router.get('/', async (req, res) => {
  try {
    const { city } = req.query;
    let sql = 'SELECT * FROM theatres';
    const params = [];
    if (city) { sql += ' WHERE city = ?'; params.push(city); }
    sql += ' ORDER BY city, name';
    const [rows] = await db.query(sql, params);
    return res.json(rows);
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch theatres.' });
  }
});

// ── GET /api/theatres/cities ─────────────────
router.get('/cities', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT DISTINCT city FROM theatres ORDER BY city');
    return res.json(rows.map(r => r.city));
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch cities.' });
  }
});

// ── GET /api/theatres/:id ────────────────────
router.get('/:id', async (req, res) => {
  try {
    const [[theatre]] = await db.query('SELECT * FROM theatres WHERE id = ?', [req.params.id]);
    if (!theatre) return res.status(404).json({ error: 'Theatre not found.' });

    const [screens] = await db.query(
      'SELECT * FROM screens WHERE theatre_id = ? ORDER BY name',
      [req.params.id]
    );
    return res.json({ ...theatre, screens });
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch theatre.' });
  }
});

module.exports = router;
