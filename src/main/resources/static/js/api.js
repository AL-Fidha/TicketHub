// ─────────────────────────────────────────────────────────────
//  frontend/js/api.js
//  All fetch() calls to the Node.js backend.
//  Every page imports this via <script src="/js/api.js">
// ─────────────────────────────────────────────────────────────

let API_BASE = '/api';
if (typeof window !== 'undefined') {
  if (window.location.protocol === 'file:' || ['5500', '3000', '5000', '8000'].includes(window.location.port)) {
    API_BASE = window.__API_BASE__ || 'http://localhost:8081/api';
  }
}

// ── token helpers ─────────────────────────────
const Auth = {
  getToken: ()       => localStorage.getItem('th_token'),
  setToken: (t)      => localStorage.setItem('th_token', t),
  setUser:  (u)      => localStorage.setItem('th_user', JSON.stringify(u)),
  getUser:  ()       => { try { return JSON.parse(localStorage.getItem('th_user')); } catch { return null; } },
  clear:    ()       => { localStorage.removeItem('th_token'); localStorage.removeItem('th_user'); },
  isAdmin:  ()       => Auth.getUser()?.role === 'admin',
  loggedIn: ()       => !!Auth.getToken(),
};

// ── core fetch wrapper ────────────────────────
async function apiFetch(path, options = {}) {
  const token = Auth.getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  } catch (err) {
    if (API_BASE.includes('8081')) {
      try {
        const altBase = 'http://localhost:8080/api';
        res = await fetch(`${altBase}${path}`, { ...options, headers });
        API_BASE = altBase;
        window.__API_BASE__ = altBase;
      } catch (err2) {
        throw new Error('Could not connect to backend server. Make sure TicketHub is running on port 8080 or 8081.');
      }
    } else {
      throw new Error('Could not connect to backend server. Make sure TicketHub is running.');
    }
  }

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    const errorMsg = data.error || data.message || `Request failed (${res.status})`;
    throw new Error(errorMsg);
  }
  return data;
}

// ── AUTH ──────────────────────────────────────
const AuthAPI = {
  login: async (email, password) => {
    const data = await apiFetch('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
    const user = data.user || { id: data.id, name: data.name, email: data.email, role: data.role };
    return { token: data.token, user };
  },
  register: async (name, email, phone, password) => {
    const data = await apiFetch('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, phone, password }) });
    const user = data.user || { id: data.id, name: data.name, email: data.email, role: data.role };
    return { token: data.token, user };
  },
  me:       ()                      => apiFetch('/auth/me'),
  updateProfile: (name, phone)      => apiFetch('/auth/profile',  { method: 'PUT',  body: JSON.stringify({ name, phone }) }),
  changePassword: (currentPassword, newPassword) => apiFetch('/auth/password', { method: 'PUT', body: JSON.stringify({ currentPassword, newPassword }) }),
};

// ── MOVIES ────────────────────────────────────
const MoviesAPI = {
  getAll:   (params = {}) => apiFetch('/movies?' + new URLSearchParams(params)),
  getOne:   (id)          => apiFetch(`/movies/${id}`),
  getShows: (id, params)  => apiFetch(`/movies/${id}/shows?` + new URLSearchParams(params)),
  add:      (data)        => apiFetch('/movies',     { method: 'POST',   body: JSON.stringify(data) }),
  update:   (id, data)    => apiFetch(`/movies/${id}`, { method: 'PUT',  body: JSON.stringify(data) }),
  delete:   (id)          => apiFetch(`/movies/${id}`, { method: 'DELETE' }),
};

// ── THEATRES ──────────────────────────────────
const TheatresAPI = {
  getAll:   (params = {}) => apiFetch('/theatres?' + new URLSearchParams(params)),
  getCities:()            => apiFetch('/theatres/cities'),
  getOne:   (id)          => apiFetch(`/theatres/${id}`),
  getShows: (id)          => apiFetch(`/theatres/${id}/shows`),
};

// ── SEATS ─────────────────────────────────────
const SeatsAPI = {
  getMap: (show_id) => apiFetch(`/seats/${show_id}`),
};

// ── BOOKINGS ──────────────────────────────────
const BookingsAPI = {
  create:  (show_id, seats)  => {
    const numericShowId = Number(show_id);
    const normalizedSeats = (seats || []).map(s => {
      if (typeof s === 'string') return { label: s.trim(), type: ['A','B'].includes(s.trim().charAt(0)) ? 'premium' : 'standard' };
      return { label: String(s.label || s).trim(), type: s.type || (['A','B'].includes(String(s.label || s).trim().charAt(0)) ? 'premium' : 'standard') };
    });
    return apiFetch('/bookings', {
      method: 'POST',
      body: JSON.stringify({
        show_id: numericShowId,
        showId: numericShowId,
        seats: normalizedSeats
      })
    });
  },
  mine:    ()                => apiFetch('/bookings/mine'),
  getOne:  (ref)             => apiFetch(`/bookings/${ref}`),
  cancel:  (ref)             => apiFetch(`/bookings/${ref}/cancel`, { method: 'PUT' }),
  // admin
  all:     ()                => apiFetch('/bookings'),
};

// ── ADMIN ─────────────────────────────────────
const AdminAPI = {
  stats: () => apiFetch('/admin/stats'),
  users: () => apiFetch('/admin/users'),
  theatres: () => apiFetch('/admin/theatres'),
  screens: () => apiFetch('/admin/screens'),
  shows: () => apiFetch('/admin/shows'),
  addTheatre: data => apiFetch('/admin/theatres', { method: 'POST', body: JSON.stringify(data) }),
  addShow: data => apiFetch('/admin/shows', { method: 'POST', body: JSON.stringify(data) }),
  deleteShow: id => apiFetch(`/admin/shows/${id}`, { method: 'DELETE' }),
  discoverTmdbMovies: language => apiFetch(`/admin/tmdb/movies?language=${encodeURIComponent(language)}`),
  importTmdbMovie: (tmdbId, language) => apiFetch(`/admin/tmdb/movies/${tmdbId}?language=${encodeURIComponent(language)}`, { method: 'POST' }),
};

// ── TOAST ─────────────────────────────────────
function showToast(msg, type = 'success') {
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const container = document.getElementById('toastContainer');
  if (!container) return;
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span class="toast-icon">${icons[type]||'ℹ️'}</span>
                  <span class="toast-msg">${msg}</span>
                  <span class="toast-close" onclick="this.parentElement.remove()">✕</span>`;
  container.appendChild(el);
  setTimeout(() => el.remove(), 4500);
}

// ── NAV: reflect login state ──────────────────
function updateNav() {
  const user = Auth.getUser();
  const el   = document.getElementById('navActions');
  if (!el) return;
  if (user) {
    const avatarEmoji = localStorage.getItem('th_user_avatar') || '🍿';
    const adminBadge = user.role === 'admin'
      ? `<a href="/dashboard.html#manageMovies" class="nav-admin-pill" style="text-decoration:none;">🛡️ Admin</a>`
      : '';
    el.innerHTML = `
      ${adminBadge}
      <a href="/dashboard.html" class="nav-user">
        <div class="nav-avatar" style="font-size:1.1rem;background:var(--bg-card2);border:1px solid var(--border);">${avatarEmoji}</div>
        <span class="nav-username">${(user.name || 'User').split(' ')[0]}</span>
      </a>`;
  } else {
    el.innerHTML = `
      <a href="/admin-login.html" class="btn-nav-admin" title="Admin Portal">🛡️ Admin</a>
      <a href="/login.html" class="btn-nav-login">Log In</a>
      <a href="/login.html" class="btn-nav-signup btn">Sign Up</a>`;
  }
}

function initCinemaIntro() {
  const intro = document.getElementById('cinemaIntro');
  if (!intro) return;
  if (sessionStorage.getItem('th_intro_played')) {
    intro.style.display = 'none';
    return;
  }
  sessionStorage.setItem('th_intro_played', 'true');
  const hideIntro = () => {
    intro.classList.add('fade-out');
    setTimeout(() => { intro.style.display = 'none'; }, 850);
  };
  const skipBtn = document.getElementById('skipIntroBtn');
  if (skipBtn) skipBtn.addEventListener('click', hideIntro);
  setTimeout(hideIntro, 1800);
}

// ── POSTER helper (Real TMDB / Image URLs) ─────
function posterImg(movie, cls = '') {
  const url = movie.poster_url || movie.posterUrl || movie.posterPath;
  const title = String(movie.title || 'Untitled');
  const safeTitle = title.replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[character]);
  const monogram = title.trim().split(/\s+/).slice(0, 2).map(word => word[0] || '').join('').toUpperCase();
  const fallback = `<div class="poster-fallback" style="display:none" role="img" aria-label="Poster unavailable for ${safeTitle}">
    <span class="poster-monogram">${monogram}</span>
    <span class="poster-fallback-title">${safeTitle}</span>
    <span class="poster-fallback-note">Artwork unavailable</span>
  </div>`;
  if (!url) return fallback.replace('display:none', 'display:flex');
  const safeUrl = String(url).replace(/[&"']/g, character => ({ '&': '&amp;', '"': '&quot;', "'": '&#39;' })[character]);
  return `<img src="${safeUrl}" alt="${safeTitle}" class="poster-img ${cls}" loading="lazy"
            onerror="this.style.display='none';this.nextElementSibling.style.display='flex';" />${fallback}`;
}

function genEmoji(genre) {
  const map = { Action:'💥', Thriller:'🔍', Crime:'🚔', Drama:'🎭', Horror:'👻',
                Comedy:'😄', Romance:'💕', 'Sci-Fi':'🚀', Adventure:'🗺️',
                Sport:'🏆', Mystery:'🕵️', War:'⚔️', History:'📜', Fantasy:'🪄' };
  for (const k of Object.keys(map)) {
    if (genre && genre.toLowerCase().includes(k.toLowerCase())) return map[k];
  }
  return '🎬';
}

// ── Duration formatter ────────────────────────
function fmtDuration(mins) {
  const m = Number(mins) || 0;
  return `${Math.floor(m / 60)}h ${m % 60}m`;
}

// ── Date chips helper ─────────────────────────
function renderDateChips(containerId, onSelect) {
  const days   = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
  const cont   = document.getElementById(containerId);
  if (!cont) return;
  const today  = new Date();
  cont.innerHTML = Array.from({length: 7}, (_, i) => {
    const d = new Date(today); d.setDate(today.getDate() + i);
    const iso  = d.toISOString().split('T')[0];
    const label = i === 0 ? 'Today' : i === 1 ? 'Tmrw' : days[d.getDay()];
    return `<div class="date-chip${i===0?' active':''}" data-date="${iso}" onclick="selectDateChip(this,'${containerId}')">
              <div class="dc-day">${label}</div>
              <div class="dc-num">${d.getDate()}</div>
              <div class="dc-mon">${months[d.getMonth()]}</div>
            </div>`;
  }).join('');

  // wire up chips to caller
  window[`_dateChipCb_${containerId}`] = onSelect;
  // fire for today
  const first = cont.querySelector('.date-chip');
  if (first) onSelect(first.dataset.date);
}

function selectDateChip(el, containerId) {
  document.querySelectorAll(`#${containerId} .date-chip`).forEach(c => c.classList.remove('active'));
  el.classList.add('active');
  const cb = window[`_dateChipCb_${containerId}`];
  if (cb) cb(el.dataset.date);
}

document.addEventListener('DOMContentLoaded', () => {
  const menuButton = document.getElementById('hamburger');
  const navLinks = document.querySelector('.nav-links');
  if (!menuButton || !navLinks) return;

  menuButton.setAttribute('aria-expanded', 'false');
  menuButton.setAttribute('aria-label', 'Open navigation menu');
  menuButton.addEventListener('click', () => {
    const isOpen = menuButton.getAttribute('aria-expanded') === 'true';
    menuButton.setAttribute('aria-expanded', String(!isOpen));
    menuButton.setAttribute('aria-label', isOpen ? 'Open navigation menu' : 'Close navigation menu');
    navLinks.classList.toggle('open', !isOpen);
  });
});
