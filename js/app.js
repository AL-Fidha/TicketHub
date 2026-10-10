/**
 * TICKET HUB — Core Application Logic
 * Movie Ticket Booking Management System
 * Dept. of CSE, College of Engineering, Muttathara
 */

(function () {
  'use strict';

  // ─────────────────────────────────────────────
  //  SEED DATA — Movies Catalog
  // ─────────────────────────────────────────────
  const SEED_MOVIES = [

    // ── MALAYALAM ──
    {
      id: 'mv001',
      title: 'Bethlehem Kudumba Unit (BKU)',
      genre: 'Comedy',
      language: 'Malayalam',
      duration: 157,
      rating: 8.4,
      emoji: '😄',
      color: 'c5',
      status: 'Now Showing',
      description: 'A hilarious family comedy-drama about an eccentric Bethlehem household whose members stumble into one outrageous situation after another — all while trying to keep up appearances at a grand family reunion.',
      shows: [
        { time: '10:00 AM', screen: 'Screen 1', seats: 85 },
        { time: '01:30 PM', screen: 'Screen 1', seats: 70 },
        { time: '05:00 PM', screen: 'Screen 2', seats: 60 },
        { time: '09:00 PM', screen: 'Screen 2', seats: 90 }
      ]
    },
    {
      id: 'mv002',
      title: 'Pradhama Drishtya Kuttakkar',
      genre: 'Thriller',
      language: 'Malayalam',
      duration: 161,
      rating: 8.7,
      emoji: '🔍',
      color: 'c2',
      status: 'Now Showing',
      description: 'A gripping crime mystery in which a veteran detective and an unlikely partner must unpick a web of deception before the true culprit vanishes for good. Layered, tense, and impossible to predict.',
      shows: [
        { time: '10:30 AM', screen: 'Screen 3', seats: 75 },
        { time: '02:00 PM', screen: 'Screen 3', seats: 55 },
        { time: '06:00 PM', screen: 'Screen 4', seats: 80 },
        { time: '09:30 PM', screen: 'Screen 4', seats: 95 }
      ]
    },
    {
      id: 'mv003',
      title: 'L&O: Law and Order',
      genre: 'Action',
      language: 'Malayalam',
      duration: 155,
      rating: 8.5,
      emoji: '⚖️',
      color: 'c3',
      status: 'Now Showing',
      description: 'When a corrupt nexus between law enforcement and organised crime threatens an entire district, one relentless officer decides to dismantle the system from within — badge, fists, and all.',
      shows: [
        { time: '11:00 AM', screen: 'Screen 1', seats: 65 },
        { time: '02:30 PM', screen: 'Screen 2', seats: 50 },
        { time: '06:30 PM', screen: 'Screen 1', seats: 88 },
        { time: '10:00 PM', screen: 'Screen 3', seats: 100 }
      ]
    },
    {
      id: 'mv004',
      title: "It's a Medical Miracle",
      genre: 'Thriller',
      language: 'Malayalam',
      duration: 137,
      rating: 8.2,
      emoji: '🏥',
      color: 'c6',
      status: 'Now Showing',
      description: 'Inside a high-pressure government hospital, an inexplicable series of patient recoveries sets off a tense investigation. Is it science, faith, or something far more sinister hiding behind the miracle?',
      shows: [
        { time: '10:00 AM', screen: 'Screen 4', seats: 80 },
        { time: '01:00 PM', screen: 'Screen 4', seats: 60 },
        { time: '04:30 PM', screen: 'Screen 5', seats: 72 },
        { time: '08:00 PM', screen: 'Screen 5', seats: 90 }
      ]
    },
    {
      id: 'mv005',
      title: "I'm Game",
      genre: 'Action',
      language: 'Malayalam',
      duration: 173,
      rating: 8.6,
      emoji: '🎯',
      color: 'c1',
      status: 'New',
      description: 'A fast-talking street-smart con artist gets entangled in a high-stakes criminal operation where the only way out is to play every side against the other — and win.',
      shows: [
        { time: '11:30 AM', screen: 'Screen 2', seats: 55 },
        { time: '03:30 PM', screen: 'Screen 3', seats: 70 },
        { time: '07:30 PM', screen: 'Screen 2', seats: 45 },
        { time: '10:30 PM', screen: 'Screen 3', seats: 100 }
      ]
    },

    // ── ENGLISH ──
    {
      id: 'mv006',
      title: 'Resident Evil (2026)',
      genre: 'Horror',
      language: 'English',
      duration: 120,
      rating: 7.8,
      emoji: '🧟',
      color: 'c4',
      status: 'New',
      description: 'The T-virus outbreak returns with a vengeance. A new generation of survivors must navigate the ruins of a city overrun by bio-engineered horrors, uncovering Umbrella\'s darkest secret yet.',
      shows: [
        { time: '12:00 PM', screen: 'Screen 6', seats: 85 },
        { time: '03:30 PM', screen: 'Screen 6', seats: 65 },
        { time: '07:00 PM', screen: 'Screen 7', seats: 75 },
        { time: '10:30 PM', screen: 'Screen 7', seats: 100 }
      ]
    },
    {
      id: 'mv007',
      title: 'Spider-Man: Brand New Day',
      genre: 'Action',
      language: 'English',
      duration: 145,
      rating: 9.0,
      emoji: '🕷️',
      color: 'c2',
      status: 'New',
      description: 'Peter Parker faces a city that no longer remembers him, a villain who knows his every move, and a moral dilemma that could rewrite the rules of being Spider-Man forever.',
      shows: [
        { time: '10:00 AM', screen: 'Screen 7', seats: 30 },
        { time: '01:00 PM', screen: 'Screen 8', seats: 20 },
        { time: '04:00 PM', screen: 'Screen 7', seats: 15 },
        { time: '07:30 PM', screen: 'Screen 8', seats: 40 },
        { time: '10:30 PM', screen: 'Screen 7', seats: 60 }
      ]
    },
    {
      id: 'mv008',
      title: 'Practical Magic 2',
      genre: 'Comedy',
      language: 'English',
      duration: 130,
      rating: 7.6,
      emoji: '🪄',
      color: 'c1',
      status: 'Now Showing',
      description: 'The Owens sisters are back, older and no less magical — or chaotic. When a family curse resurfaces, they must blend modern life with ancient witchcraft before it tears their world apart.',
      shows: [
        { time: '11:00 AM', screen: 'Screen 5', seats: 90 },
        { time: '02:30 PM', screen: 'Screen 6', seats: 75 },
        { time: '06:00 PM', screen: 'Screen 5', seats: 80 },
        { time: '09:00 PM', screen: 'Screen 6', seats: 95 }
      ]
    },
    {
      id: 'mv009',
      title: 'The Uprising',
      genre: 'Drama',
      language: 'English',
      duration: 128,
      rating: 8.3,
      emoji: '✊',
      color: 'c3',
      status: 'Now Showing',
      description: 'Based on true events, this sweeping historical war drama follows a band of ordinary people who sparked a revolution that changed a nation — told through the eyes of those history almost forgot.',
      shows: [
        { time: '10:30 AM', screen: 'Screen 8', seats: 70 },
        { time: '02:00 PM', screen: 'Screen 8', seats: 85 },
        { time: '05:30 PM', screen: 'Screen 1', seats: 60 },
        { time: '09:00 PM', screen: 'Screen 1', seats: 88 }
      ]
    },
    {
      id: 'mv010',
      title: 'The Odyssey',
      genre: 'Action',
      language: 'English',
      duration: 172,
      rating: 8.9,
      emoji: '🏛️',
      color: 'c6',
      status: 'New',
      description: 'A breathtaking epic retelling of Homer\'s classic. Odysseus must battle gods, monsters, and his own pride on a decade-long voyage home — reimagined with stunning spectacle and raw humanity.',
      shows: [
        { time: '11:30 AM', screen: 'Screen 2', seats: 40 },
        { time: '03:00 PM', screen: 'Screen 3', seats: 35 },
        { time: '07:00 PM', screen: 'Screen 2', seats: 50 },
        { time: '10:30 PM', screen: 'Screen 3', seats: 75 }
      ]
    },

    // ── TAMIL ──
    {
      id: 'mv011',
      title: 'Mandaadi',
      genre: 'Action',
      language: 'Tamil',
      duration: 154,
      rating: 8.1,
      emoji: '🏆',
      color: 'c5',
      status: 'Now Showing',
      description: 'A rough-edged rural athlete dreams of glory on the national stage. What follows is an electrifying sports drama filled with dust, grit, tears, and the unyielding spirit of a champion in the making.',
      shows: [
        { time: '10:00 AM', screen: 'Screen 4', seats: 80 },
        { time: '01:30 PM', screen: 'Screen 5', seats: 65 },
        { time: '05:00 PM', screen: 'Screen 4', seats: 75 },
        { time: '09:00 PM', screen: 'Screen 5', seats: 90 }
      ]
    },
    {
      id: 'mv012',
      title: 'Sardar 2',
      genre: 'Action',
      language: 'Tamil',
      duration: 150,
      rating: 8.5,
      emoji: '🕵️',
      color: 'c2',
      status: 'New',
      description: 'The legendary double-agent returns. With two identities, one mission, and enemies on every side, Sardar must expose a global conspiracy before it destroys everything he has ever protected.',
      shows: [
        { time: '10:30 AM', screen: 'Screen 6', seats: 55 },
        { time: '02:00 PM', screen: 'Screen 7', seats: 45 },
        { time: '05:30 PM', screen: 'Screen 6', seats: 70 },
        { time: '09:30 PM', screen: 'Screen 7', seats: 95 }
      ]
    },
    {
      id: 'mv013',
      title: 'Sandakkari',
      genre: 'Comedy',
      language: 'Tamil',
      duration: 141,
      rating: 7.7,
      emoji: '💍',
      color: 'c1',
      status: 'Now Showing',
      description: 'A jovial, large-hearted man agrees to an arranged marriage for the sake of his family — but the wedding planning spirals into the most lovably chaotic comedy of the year.',
      shows: [
        { time: '11:00 AM', screen: 'Screen 8', seats: 85 },
        { time: '03:00 PM', screen: 'Screen 8', seats: 70 },
        { time: '07:00 PM', screen: 'Screen 1', seats: 80 },
        { time: '10:00 PM', screen: 'Screen 1', seats: 95 }
      ]
    },
    {
      id: 'mv014',
      title: 'Hi',
      genre: 'Romance',
      language: 'Tamil',
      duration: 144,
      rating: 7.9,
      emoji: '💕',
      color: 'c3',
      status: 'Now Showing',
      description: 'A chance encounter, a single word, and a lifetime\'s worth of feelings. This warm romantic comedy-drama explores love, family expectations, and what it truly means to say hello to someone new.',
      shows: [
        { time: '12:00 PM', screen: 'Screen 2', seats: 90 },
        { time: '04:00 PM', screen: 'Screen 3', seats: 78 },
        { time: '08:00 PM', screen: 'Screen 2', seats: 65 },
        { time: '10:30 PM', screen: 'Screen 3', seats: 100 }
      ]
    },
    {
      id: 'mv015',
      title: 'Vishwanath and Sons',
      genre: 'Drama',
      language: 'Tamil',
      duration: 161,
      rating: 8.8,
      emoji: '👨‍👦',
      color: 'c4',
      status: 'New',
      description: 'Three generations of a sporting family. One unfinished legacy. A feel-good, deeply human story about fathers, sons, sacrifice, and the quiet victories that never make the scoreboard.',
      shows: [
        { time: '10:00 AM', screen: 'Screen 4', seats: 75 },
        { time: '01:30 PM', screen: 'Screen 5', seats: 60 },
        { time: '05:30 PM', screen: 'Screen 4', seats: 82 },
        { time: '09:00 PM', screen: 'Screen 5', seats: 90 }
      ]
    }

  ];

  // ─────────────────────────────────────────────
  //  STORAGE HELPERS
  // ─────────────────────────────────────────────
  const KEYS = {
    users: 'th_users',
    movies: 'th_movies',
    bookings: 'th_bookings',
    session: 'th_session',
    seeded: 'th_seeded'
  };

  function store(key, value) {
    try { localStorage.setItem(key, JSON.stringify(value)); } catch (e) { console.error('Storage error:', e); }
  }

  function retrieve(key, fallback = null) {
    try {
      const v = localStorage.getItem(key);
      return v ? JSON.parse(v) : fallback;
    } catch (e) { return fallback; }
  }

  // ─────────────────────────────────────────────
  //  SEED INITIALIZATION
  // ─────────────────────────────────────────────
  function initSeed() {
    // Version stamp — bump this to force a re-seed when movie data changes
    const SEED_VERSION = 'v3';
    if (retrieve(KEYS.seeded) !== SEED_VERSION) {
      const demoEmails = ['demo@tickethub.com', 'admin@tickethub.com'];
      store(KEYS.users, getUsers().filter(user => !demoEmails.includes(user.email.toLowerCase())));
      store(KEYS.bookings, retrieve(KEYS.bookings, []).filter(booking => !demoEmails.includes((booking.email || '').toLowerCase())));
      if (!retrieve(KEYS.movies)) store(KEYS.movies, SEED_MOVIES);
      store(KEYS.seeded, SEED_VERSION);
    }
  }

  // ─────────────────────────────────────────────
  //  USER MANAGEMENT
  // ─────────────────────────────────────────────
  function getUsers() {
    return retrieve(KEYS.users, []);
  }

  function getAllUsers() {
    return retrieve(KEYS.users, []);
  }

  function getCurrentUser() {
    const sessionEmail = retrieve(KEYS.session);
    if (!sessionEmail) return null;
    const users = getUsers();
    return users.find(u => u.email === sessionEmail) || null;
  }

  function login(email, password) {
    const users = getUsers();
    const user = users.find(u => u.email.toLowerCase() === email.toLowerCase());
    if (!user) return { success: false, message: 'No account found with that email.' };
    if (user.password !== password) return { success: false, message: 'Incorrect password.' };
    store(KEYS.session, user.email);
    return { success: true, user };
  }

  function register(data) {
    const users = getUsers();
    if (users.find(u => u.email.toLowerCase() === data.email.toLowerCase())) {
      return { success: false, message: 'An account with this email already exists.' };
    }
    const newUser = {
      id: 'u_' + Date.now(),
      name: data.name,
      email: data.email,
      phone: data.phone || '',
      password: data.password,
      role: data.role || 'customer'
    };
    users.push(newUser);
    store(KEYS.users, users);
    store(KEYS.session, newUser.email);
    return { success: true, user: newUser };
  }

  function logout() {
    localStorage.removeItem(KEYS.session);
  }

  function updateUser(updated) {
    const users = getUsers();
    const idx = users.findIndex(u => u.id === updated.id);
    if (idx >= 0) {
      users[idx] = { ...users[idx], ...updated };
      store(KEYS.users, users);
      store(KEYS.session, updated.email);
    }
  }

  function changePassword(email, currentPwd, newPwd) {
    const users = getUsers();
    const user = users.find(u => u.email === email);
    if (!user) return { success: false, message: 'User not found.' };
    if (user.password !== currentPwd) return { success: false, message: 'Current password is incorrect.' };
    user.password = newPwd;
    store(KEYS.users, users);
    return { success: true };
  }

  // ─────────────────────────────────────────────
  //  MOVIE MANAGEMENT
  // ─────────────────────────────────────────────
  function getMovies() {
    return retrieve(KEYS.movies, []);
  }

  function saveAdminMovie(movie) {
    const movies = getMovies();
    const idx = movies.findIndex(m => m.id === movie.id);
    if (idx >= 0) {
      movies[idx] = { ...movies[idx], ...movie };
    } else {
      movies.push(movie);
    }
    store(KEYS.movies, movies);
  }

  function deleteMovie(id) {
    const movies = getMovies().filter(m => m.id !== id);
    store(KEYS.movies, movies);
  }

  // ─────────────────────────────────────────────
  //  BOOKING MANAGEMENT
  // ─────────────────────────────────────────────
  function getAllBookings() {
    return retrieve(KEYS.bookings, []);
  }

  function getUserBookings(email) {
    return getAllBookings()
      .filter(b => b.email === email)
      .sort((a, b) => new Date(b.bookedAt) - new Date(a.bookedAt));
  }

  function saveBooking(record) {
    const bookings = getAllBookings();
    bookings.unshift(record);
    store(KEYS.bookings, bookings);
  }

  function cancelBooking(id) {
    const bookings = getAllBookings();
    const b = bookings.find(bk => bk.id === id);
    if (b) { b.status = 'Cancelled'; store(KEYS.bookings, bookings); }
  }

  // ─────────────────────────────────────────────
  //  MOVIE CARD RENDERER
  // ─────────────────────────────────────────────
  function renderMovieCard(movie, showDetail = false) {
    const minSeats = Math.min(...movie.shows.map(s => s.seats));
    const urgency = minSeats < 30 ? `<span style="color:#f59e0b;font-size:0.72rem;">🔥 ${minSeats} seats left</span>` : `<span style="color:var(--accent-green);font-size:0.72rem;">✅ Available</span>`;

    return `
      <div class="movie-card" onclick="${showDetail ? `openMovieDetail('${movie.id}')` : `window.location.href='booking.html?movie=${movie.id}'`}">
        <div class="movie-poster">
          <div class="poster-placeholder ${movie.color}">${movie.emoji}</div>
          <div class="movie-rating">⭐ ${movie.rating}</div>
          ${movie.status === 'New' ? '<div class="movie-badge">New</div>' : ''}
          <div class="movie-overlay">
            <button class="overlay-btn">${showDetail ? 'View Details' : 'Book Now'}</button>
          </div>
        </div>
        <div class="movie-info">
          <div class="movie-title">${movie.title}</div>
          <div class="movie-genre">${movie.genre} · ${movie.duration} min</div>
          <div class="movie-meta">
            <span class="movie-lang">${movie.language}</span>
            ${urgency}
          </div>
        </div>
      </div>
    `;
  }

  // ─────────────────────────────────────────────
  //  NAV UPDATE (reflect login state)
  // ─────────────────────────────────────────────
  function updateNav() {
    const user = getCurrentUser();
    const navActions = document.getElementById('navActions');
    if (!navActions) return;

    if (user) {
      const initials = user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
      navActions.innerHTML = `
        <a href="dashboard.html" class="nav-user">
          <div class="nav-avatar">${initials}</div>
          <span class="nav-username">${user.name.split(' ')[0]}</span>
        </a>
      `;
    } else {
      navActions.innerHTML = `
        <a href="login.html" class="btn-nav-login">Log In</a>
        <a href="login.html" class="btn-nav-signup btn">Sign Up</a>
      `;
    }
  }

  // ─────────────────────────────────────────────
  //  TOAST NOTIFICATIONS
  // ─────────────────────────────────────────────
  function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <span class="toast-icon">${icons[type] || 'ℹ️'}</span>
      <span class="toast-msg">${message}</span>
      <span class="toast-close" onclick="this.parentElement.remove()">✕</span>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
  }

  // ─────────────────────────────────────────────
  //  HAMBURGER MENU
  // ─────────────────────────────────────────────
  document.addEventListener('DOMContentLoaded', () => {
    const hamburger = document.getElementById('hamburger');
    const navLinks = document.querySelector('.nav-links');
    if (hamburger && navLinks) {
      hamburger.addEventListener('click', () => {
        const isOpen = navLinks.style.display === 'flex';
        navLinks.style.cssText = isOpen
          ? ''
          : 'display:flex;flex-direction:column;position:fixed;top:68px;left:0;right:0;background:var(--bg-nav);padding:1rem 2rem;gap:0.5rem;border-bottom:1px solid var(--border);z-index:999;backdrop-filter:blur(12px);';
      });
    }
  });

  // ─────────────────────────────────────────────
  //  PUBLIC API
  // ─────────────────────────────────────────────
  initSeed();

  window.TicketHub = {
    // Users
    getCurrentUser,
    getAllUsers,
    login,
    register,
    logout,
    updateUser,
    changePassword,
    // Movies
    getMovies,
    saveAdminMovie,
    deleteMovie,
    // Bookings
    getAllBookings,
    getUserBookings,
    saveBooking,
    cancelBooking,
    // UI helpers
    renderMovieCard,
    updateNav,
    showToast
  };

}());
