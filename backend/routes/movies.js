// ─────────────────────────────────────────────────────────────
//  backend/routes/movies.js
//  GET  /api/movies                 — all movies (with TMDB poster)
//  GET  /api/movies/:id             — single movie
//  GET  /api/movies/:id/shows       — shows for a movie (by date)
//  POST /api/movies                 — admin: add movie
//  PUT  /api/movies/:id             — admin: update movie
//  DELETE /api/movies/:id           — admin: delete movie
// ─────────────────────────────────────────────────────────────
require('dotenv').config({ path: require('path').join(__dirname, '../../.env') });

const express  = require('express');
const fetch    = require('node-fetch');
const db       = require('../config/db');
const { requireAuth, requireAdmin } = require('../middleware/auth');

const router = express.Router();

const TMDB_KEY   = process.env.TMDB_API_KEY;
const TMDB_BASE  = process.env.TMDB_BASE_URL  || 'https://api.themoviedb.org/3';
const IMG_BASE   = process.env.TMDB_IMAGE_BASE || 'https://image.tmdb.org/t/p/w500';

// ── TMDB helper: get poster_path by title ────
async function fetchTMDBPoster(title, tmdb_id) {
  try {
    let path = null;

    if (tmdb_id) {
      const r = await fetch(`${TMDB_BASE}/movie/${tmdb_id}?api_key=${TMDB_KEY}`);
      const d = await r.json();
      path = d.poster_path || null;
    } else {
      const q = encodeURIComponent(title);
      const r = await fetch(`${TMDB_BASE}/search/movie?api_key=${TMDB_KEY}&query=${q}`);
      const d = await r.json();
      if (d.results && d.results.length > 0) path = d.results[0].poster_path;
    }
    return path;
  } catch {
    return null;
  }
}

// ── attach full poster URL to each movie ─────
function withPoster(movie) {
  return {
    ...movie,
    poster_url: movie.poster_path ? `${IMG_BASE}${movie.poster_path}` : null
  };
}

// ── GET /api/movies ──────────────────────────
router.get('/', async (req, res) => {
  try {
    const { language, genre, search } = req.query;
    let sql = 'SELECT * FROM movies WHERE 1=1';
    const params = [];

    if (language) { sql += ' AND language = ?'; params.push(language); }
    if (genre)    { sql += ' AND genre LIKE ?'; params.push(`%${genre}%`); }
    if (search)   { sql += ' AND title LIKE ?'; params.push(`%${search}%`); }
    sql += ' ORDER BY status DESC, title ASC';

    const [movies] = await db.query(sql, params);

    // Fetch & cache missing posters
    const updated = await Promise.all(movies.map(async (m) => {
      if (!m.poster_path && TMDB_KEY && TMDB_KEY !== 'your_tmdb_api_key_here') {
        const path = await fetchTMDBPoster(m.title, m.tmdb_id);
        if (path) {
          await db.query('UPDATE movies SET poster_path = ? WHERE id = ?', [path, m.id]);
          m.poster_path = path;
        }
      }
      return withPoster(m);
    }));

    return res.json(updated);
  } catch (err) {
    console.error('[GET /movies]', err);
    return res.status(500).json({ error: 'Could not fetch movies.' });
  }
});

// ── GET /api/movies/:id ──────────────────────
router.get('/:id', async (req, res) => {
  try {
    const [rows] = await db.query('SELECT * FROM movies WHERE id = ?', [req.params.id]);
    if (!rows.length) return res.status(404).json({ error: 'Movie not found.' });

    const movie = rows[0];
    if (!movie.poster_path && TMDB_KEY && TMDB_KEY !== 'your_tmdb_api_key_here') {
      const path = await fetchTMDBPoster(movie.title, movie.tmdb_id);
      if (path) {
        await db.query('UPDATE movies SET poster_path = ? WHERE id = ?', [path, movie.id]);
        movie.poster_path = path;
      }
    }
    return res.json(withPoster(movie));
  } catch (err) {
    return res.status(500).json({ error: 'Could not fetch movie.' });
  }
});

// ── GET /api/movies/:id/shows ────────────────
router.get('/:id/shows', async (req, res) => {
  try {
    const { date, theatre_id } = req.query;
    let sql = `
      SELECT sh.id AS show_id, sh.show_date, sh.show_time,
             sh.price_std, sh.price_prem,
             sc.id AS screen_id, sc.name AS screen_name,
             sc.rows, sc.cols, sc.total_seats,
             t.id AS theatre_id, t.name AS theatre_name,
             t.location, t.city,
             (SELECT COUNT(*) FROM booking_seats bs
              JOIN bookings bk ON bk.id = bs.booking_id
              WHERE bs.show_id = sh.id AND bk.status != 'Cancelled')
             AS seats_booked
      FROM shows sh
      JOIN screens sc  ON sc.id = sh.screen_id
      JOIN theatres t  ON t.id  = sc.theatre_id
      WHERE sh.movie_id = ?
    `;
    const params = [req.params.id];

    if (date)       { sql += ' AND sh.show_date = ?';      params.push(date); }
    if (theatre_id) { sql += ' AND t.id = ?';              params.push(theatre_id); }
    sql += ' AND sh.show_date >= CURDATE() ORDER BY sh.show_date, t.city, sh.show_time';

    const [shows] = await db.query(sql, params);
    return res.json(shows);
  } catch (err) {
    console.error('[GET /movies/:id/shows]', err);
    return res.status(500).json({ error: 'Could not fetch shows.' });
  }
});

// ── POST /api/movies  (admin) ────────────────
router.post('/', requireAuth, requireAdmin, async (req, res) => {
  const { title, genre, language, duration_min, rating, description, tmdb_id, status } = req.body;
  if (!title || !genre || !language || !duration_min)
    return res.status(400).json({ error: 'title, genre, language, duration_min are required.' });

  try {
    let poster_path = null;
    if (TMDB_KEY && TMDB_KEY !== 'your_tmdb_api_key_here') {
      poster_path = await fetchTMDBPoster(title, tmdb_id || null);
    }

    const [result] = await db.query(
      `INSERT INTO movies (title, genre, language, duration_min, rating, description, tmdb_id, poster_path, status)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [title, genre, language, duration_min, rating || 0, description || '', tmdb_id || null, poster_path, status || 'Now Showing']
    );
    return res.status(201).json({ id: result.insertId, message: 'Movie added.' });
  } catch (err) {
    console.error('[POST /movies]', err);
    return res.status(500).json({ error: 'Could not add movie.' });
  }
});

// ── PUT /api/movies/:id  (admin) ─────────────
router.put('/:id', requireAuth, requireAdmin, async (req, res) => {
  const { title, genre, language, duration_min, rating, description, status } = req.body;
  try {
    await db.query(
      `UPDATE movies SET title=?, genre=?, language=?, duration_min=?, rating=?, description=?, status=?
       WHERE id = ?`,
      [title, genre, language, duration_min, rating, description, status, req.params.id]
    );
    return res.json({ message: 'Movie updated.' });
  } catch (err) {
    return res.status(500).json({ error: 'Could not update movie.' });
  }
});

// ── DELETE /api/movies/:id  (admin) ──────────
router.delete('/:id', requireAuth, requireAdmin, async (req, res) => {
  try {
    await db.query('DELETE FROM movies WHERE id = ?', [req.params.id]);
    return res.json({ message: 'Movie deleted.' });
  } catch (err) {
    return res.status(500).json({ error: 'Could not delete movie.' });
  }
});

module.exports = router;
