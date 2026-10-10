// ─────────────────────────────────────────────────────────────
//  backend/server.js  —  Ticket Hub API Entry Point
//  Run: node server.js  or  npm run dev
// ─────────────────────────────────────────────────────────────
require('dotenv').config({ path: require('path').join(__dirname, '../.env') });

const express  = require('express');
const cors     = require('cors');
const path     = require('path');

const app = express();

// ── Middleware ────────────────────────────────
app.use(cors({ origin: '*' }));   // allow frontend (same machine, different port or file://)
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ── Serve frontend statically ─────────────────
// When you open http://localhost:5000 it serves the frontend folder
app.use(express.static(path.join(__dirname, '../frontend')));

// ── API Routes ────────────────────────────────
app.use('/api/auth',     require('./routes/auth'));
app.use('/api/movies',   require('./routes/movies'));
app.use('/api/theatres', require('./routes/theatres'));
app.use('/api/bookings', require('./routes/bookings'));
app.use('/api/seats',    require('./routes/seats'));
app.use('/api/admin',    require('./routes/admin'));

// ── Catch-all: serve frontend for any non-API route ──
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../frontend', 'index.html'));
});

// ── 404 handler for API ───────────────────────
app.use('/api', (req, res) => {
  res.status(404).json({ error: 'API endpoint not found.' });
});

// ── Global error handler ──────────────────────
app.use((err, req, res, next) => {
  console.error('[Unhandled Error]', err);
  res.status(500).json({ error: 'Internal server error.' });
});

// ── Start ─────────────────────────────────────
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`\n🎬  Ticket Hub running at http://localhost:${PORT}`);
  console.log(`📡  API base: http://localhost:${PORT}/api`);
  console.log(`🗄️   DB: ${process.env.DB_NAME}@${process.env.DB_HOST}\n`);
});
