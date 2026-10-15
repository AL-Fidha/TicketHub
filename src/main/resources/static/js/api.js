// ─────────────────────────────────────────────────────────────
//  TicketHub — Unified API Client & Standalone Resilience Engine
//  Features:
//    - Automatic connection to live Spring Boot backend (port 8081 / 8080)
//    - Seamless Standalone / Offline Fallback when server is not running
//    - LocalStorage persistence for added/deleted movies and bookings
// ─────────────────────────────────────────────────────────────

let API_BASE = '/api';
if (typeof window !== 'undefined') {
  if (window.location.protocol === 'file:' || (window.location.port !== '8081' && window.location.port !== '8080')) {
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

// ── Offline Seed Data ─────────────────────────
const DEFAULT_SEED_MOVIES = [
  { id: 1, title: 'Manjummel Boys', language: 'Malayalam', genre: 'Adventure, Thriller, Survival', duration_min: 135, rating: 8.6, poster_url: 'https://image.tmdb.org/t/p/w500/bswrtewwthpsh6nABiqKevU4UBI.jpg', description: 'A group of close friends from Kochi embark on a trip to Kodaikanal where a harrowing cave accident tests their friendship and resilience.', status: 'NOW_SHOWING' },
  { id: 2, title: 'Aavesham', language: 'Malayalam', genre: 'Action, Comedy', duration_min: 158, rating: 8.4, poster_url: 'https://image.tmdb.org/t/p/w500/k5RWPaNjgRcNvGoawYaQHQwyctI.jpg', description: 'Three engineering freshmen in Bangalore befriend an eccentric local gangster named Ranga to stand up against college bullies.', status: 'NOW_SHOWING' },
  { id: 3, title: 'Premalu', language: 'Malayalam', genre: 'Romance, Comedy', duration_min: 156, rating: 8.2, poster_url: 'https://image.tmdb.org/t/p/w500/uPpmBjY3znUqGY8kYwI5xvOrSc0.jpg', description: 'Sachin moves to Hyderabad with dreams of higher education and finds his life turned upside down when he falls for the confident Reenu.', status: 'NOW_SHOWING' },
  { id: 4, title: 'Bramayugam', language: 'Malayalam', genre: 'Horror, Mystery, Thriller', duration_min: 139, rating: 8.5, poster_url: 'https://image.tmdb.org/t/p/w500/snQLwRrfQAl5YFKVefZq9Lbscki.jpg', description: 'A fleeing folk singer in 17th-century Malabar seeks shelter in a shadowy mana, only to discover a sinister game of ancient sorcery.', status: 'NOW_SHOWING' },
  { id: 5, title: 'The Goat Life (Aadujeevitham)', language: 'Malayalam', genre: 'Adventure, Drama, Survival', duration_min: 172, rating: 8.7, poster_url: 'https://image.tmdb.org/t/p/w500/l7z3Skv1xLASnUt0whC4IInaXt0.jpg', description: 'Based on the acclaimed true story, Najeeb endures years of forced isolation and desert labor in a desperate quest for freedom.', status: 'NOW_SHOWING' },
  { id: 6, title: 'Kishkindha Kaandam', language: 'Malayalam', genre: 'Mystery, Thriller', duration_min: 125, rating: 8.5, poster_url: 'https://image.tmdb.org/t/p/w500/rniBYHDRs6e8qMkPr4D4ZE5uMhF.jpg', description: 'Strange occurrences in a monkey-inhabited reserve forest unravel deep family secrets and missing firearms in an intricate puzzle.', status: 'NOW_SHOWING' },
  { id: 7, title: 'ARM (Ajayante Randam Moshanam)', language: 'Malayalam', genre: 'Action, Adventure, Fantasy', duration_min: 147, rating: 8.1, poster_url: 'https://image.tmdb.org/t/p/w500/6OgA9x6jvwBIj8e7x8LANTbVvR2.jpg', description: 'Set in Northern Kerala across three generations, three heroes battle to protect a celestial treasure entrusted to their land.', status: 'NOW_SHOWING' },
  { id: 8, title: 'Turbo', language: 'Malayalam', genre: 'Action, Comedy', duration_min: 152, rating: 7.8, poster_url: 'https://image.tmdb.org/t/p/w500/inTKQni4YW8syrfgnXHwzmNeSo4.jpg', description: 'Turbo Jose, an unyielding man from Idukki, lands in Chennai and gets entangled in a massive corporate and underworld conspiracy.', status: 'NOW_SHOWING' },
  { id: 9, title: 'Dune: Part Two', language: 'English', genre: 'Sci-Fi, Adventure, Action', duration_min: 166, rating: 8.9, poster_url: 'https://image.tmdb.org/t/p/w500/6izwz7rsy95ARzTR3poZ8H6c5pp.jpg', description: 'Paul Atreides unites with Chani and the Fremen while seeking vengeance against the conspirators who destroyed his house.', status: 'NOW_SHOWING' },
  { id: 10, title: 'Oppenheimer', language: 'English', genre: 'Biography, Drama, History', duration_min: 180, rating: 8.9, poster_url: 'https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg', description: 'The compelling story of theoretical physicist J. Robert Oppenheimer, whose leadership of the Manhattan Project forever altered world history.', status: 'NOW_SHOWING' },
  { id: 11, title: 'Deadpool & Wolverine', language: 'English', genre: 'Action, Comedy, Sci-Fi', duration_min: 128, rating: 8.5, poster_url: 'https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg', description: 'Wade Wilson\'s quiet retirement is shattered when the TVA pulls him into an interdimensional mission requiring the claws of Wolverine.', status: 'NOW_SHOWING' },
  { id: 12, title: 'Interstellar', language: 'English', genre: 'Sci-Fi, Adventure, Drama', duration_min: 169, rating: 8.7, poster_url: 'https://image.tmdb.org/t/p/w500/yQvGrMoipbRoddT0ZR8tPoR7NfX.jpg', description: 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity\'s survival.', status: 'NOW_SHOWING' },
  { id: 13, title: 'The Batman', language: 'English', genre: 'Action, Crime, Drama', duration_min: 176, rating: 8.3, poster_url: 'https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg', description: 'When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate the city\'s hidden corruption.', status: 'NOW_SHOWING' },
  { id: 14, title: 'Gladiator II', language: 'English', genre: 'Action, Adventure, Drama', duration_min: 148, rating: 8.4, poster_url: 'https://image.tmdb.org/t/p/w500/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg', description: 'Decades after Maximus, Lucius enters the Colosseum to fight for the future of Rome and restore glory to its people.', status: 'NOW_SHOWING' },
  { id: 15, title: 'Avatar: The Way of Water', language: 'English', genre: 'Sci-Fi, Adventure, Action', duration_min: 192, rating: 8.6, poster_url: 'https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg', description: 'Jake Sully lives with his newfound family formed on the extrasolar moon Pandora once more facing a familiar threat.', status: 'NOW_SHOWING' },
  { id: 16, title: 'Leo', language: 'Tamil', genre: 'Action, Crime, Thriller', duration_min: 164, rating: 8.0, poster_url: 'https://image.tmdb.org/t/p/w500/gSOVog7ydsaF1YpgAqBqnKYFGY.jpg', description: 'Parthiban lives a quiet life in Theog until a violent incident exposes him to an international cartel seeking a long-lost operative.', status: 'NOW_SHOWING' },
  { id: 17, title: 'Jailer', language: 'Tamil', genre: 'Action, Comedy, Crime', duration_min: 168, rating: 8.2, poster_url: 'https://image.tmdb.org/t/p/w500/upwdEyhhYZ7pT2hD535fr2MGfpx.jpg', description: 'A retired jailer goes on a ruthless manhunt to track down the gangsters who kidnapped his son.', status: 'NOW_SHOWING' },
  { id: 18, title: 'Amaran', language: 'Tamil', genre: 'Action, Biography, Drama', duration_min: 169, rating: 8.8, poster_url: 'https://image.tmdb.org/t/p/w500/eCB06m1KUGilEOlIzb40nkQhVY0.jpg', description: 'The inspiring and courageous journey of Major Mukund Varadarajan and his Rajput Regiment brothers in counter-insurgency operations.', status: 'NOW_SHOWING' },
  { id: 19, title: 'Maharaja', language: 'Tamil', genre: 'Action, Crime, Thriller', duration_min: 140, rating: 8.5, poster_url: 'https://image.tmdb.org/t/p/w500/s0m4TM1XRAftQStgKpw024RvkJo.jpg', description: 'A quiet barber reports his dustbin stolen to the police, sparking a gripping investigation that conceals an intensely personal crusade.', status: 'NOW_SHOWING' },
  { id: 20, title: 'Vikram', language: 'Tamil', genre: 'Action, Thriller, Crime', duration_min: 175, rating: 8.6, poster_url: 'https://image.tmdb.org/t/p/w500/774UV1aCURb4s4JfEFg3IEMu5Zj.jpg', description: 'A black-ops commander investigates a series of murders committed by a masked vigilante group targeting a brutal drug kingpin.', status: 'NOW_SHOWING' },
  { id: 21, title: 'Stree 2', language: 'Hindi', genre: 'Comedy, Horror', duration_min: 149, rating: 8.4, poster_url: 'https://image.tmdb.org/t/p/w500/nfnhwfUEFuSOxxf4jDdBlY6Lccw.jpg', description: 'The residents of Chanderi confront a terrifying headless demon known as Sarkata, requiring an unlikely union of old friends.', status: 'NOW_SHOWING' },
  { id: 22, title: 'Jawan', language: 'Hindi', genre: 'Action, Thriller', duration_min: 169, rating: 8.3, poster_url: 'https://image.tmdb.org/t/p/w500/jFt1gS4BGHlK8xt76Y81Alp4dbt.jpg', description: 'An enigmatic vigilante orchestrates high-stakes operations across the country to expose corruption and bring justice to ordinary citizens.', status: 'NOW_SHOWING' },
  { id: 23, title: '12th Fail', language: 'Hindi', genre: 'Biography, Drama', duration_min: 147, rating: 9.1, poster_url: 'https://image.tmdb.org/t/p/w500/eebUPRI4Z5e1Z7Hev4JZAwMIFkX.jpg', description: 'Based on true events, Manoj Kumar Sharma battles extreme poverty and setbacks to restart his journey towards clearing the UPSC exam.', status: 'NOW_SHOWING' },
  { id: 24, title: 'Animal', language: 'Hindi', genre: 'Action, Crime, Drama', duration_min: 201, rating: 8.0, poster_url: 'https://image.tmdb.org/t/p/w500/6pzI1DIlxwAFffw9OZcG6cr031U.jpg', description: 'An all-consuming devotion between father and son spirals into a brutal underworld conflict when an assassination attempt occurs.', status: 'NOW_SHOWING' },
  { id: 25, title: 'Fighter', language: 'Hindi', genre: 'Action, Thriller, War', duration_min: 166, rating: 8.1, poster_url: 'https://image.tmdb.org/t/p/w500/dJPT2Uf12X6aFg8i5N67NTpIZ9t.jpg', description: 'Top Indian Air Force aviators come together to form Air Dragons, an elite aerial combat unit responding to militant threats.', status: 'NOW_SHOWING' },
  { id: 26, title: 'Bhool Bhulaiyaa 3', language: 'Hindi', genre: 'Comedy, Horror', duration_min: 158, rating: 8.0, poster_url: 'https://image.tmdb.org/t/p/w500/3AfHD1HoaQpQwKH8kxRdBKVmzeU.jpg', description: 'Rooh Baba travels to the royal kingdom of Raktaghat in West Bengal and comes face-to-face with two vengeful spirits.', status: 'NOW_SHOWING' },
  { id: 27, title: 'Kalki 2898 AD', language: 'Telugu', genre: 'Sci-Fi, Action, Mythology', duration_min: 181, rating: 8.2, poster_url: 'https://image.tmdb.org/t/p/w500/87r6wD1Z1zI5v7wL12F7c8j9v8e.jpg', description: 'A modern avatar of Vishnu descends upon Earth to protect the world from evil forces in a dystopian post-apocalyptic future.', status: 'NOW_SHOWING' }
];

const DEFAULT_SEED_THEATRES = [
  { id: 1, name: 'PVR Cinemas - LuLu Mall', location: 'LuLu Mall, NH Bypass, Edapally', city: 'Kochi', total_screens: 9 },
  { id: 2, name: 'INOX - Oberon Mall', location: 'Oberon Mall, NH 66, Edapally', city: 'Kochi', total_screens: 5 },
  { id: 3, name: 'Cinepolis - Centre Square Mall', location: 'Centre Square Mall, MG Road', city: 'Kochi', total_screens: 11 },
  { id: 4, name: 'PVR - Gold Souk Grande Mall', location: 'Gold Souk Grande Mall, Kalamassery', city: 'Kochi', total_screens: 4 },
  { id: 5, name: 'Carnival Cinemas - Kochi', location: 'Vyttila Hub, Vyttila', city: 'Kochi', total_screens: 3 },
  { id: 6, name: 'INOX - Trivandrum', location: 'Bhavani Centre, Pattom', city: 'Thiruvananthapuram', total_screens: 4 },
  { id: 7, name: 'PVR - Trivandrum', location: 'Oberon Mall, Kowdiar', city: 'Thiruvananthapuram', total_screens: 6 },
  { id: 8, name: 'Carnival Cinemas - East Fort', location: 'East Fort, Thiruvananthapuram', city: 'Thiruvananthapuram', total_screens: 4 },
  { id: 9, name: 'Miraj Cinemas - Kozhikode', location: 'Focus Mall, Mavoor Road', city: 'Kozhikode', total_screens: 4 },
  { id: 10, name: 'PVR - Kozhikode', location: 'HiLite Mall, NH 66, Kozhikode', city: 'Kozhikode', total_screens: 8 },
  { id: 11, name: 'Cinepolis - Calicut', location: 'City Centre Mall, Mavoor Road', city: 'Kozhikode', total_screens: 5 },
  { id: 12, name: 'Carnival Cinemas - Thrissur', location: 'Sobha City Mall, Thrissur', city: 'Thrissur', total_screens: 6 },
  { id: 13, name: 'INOX - Thrissur', location: 'Pearl Shopping Mall, Thrissur', city: 'Thrissur', total_screens: 4 },
  { id: 14, name: 'PVR - Kollam', location: 'KSRTC Complex, Kollam', city: 'Kollam', total_screens: 5 },
  { id: 15, name: 'Carnival Cinemas - Kannur', location: 'Focus Mall, Kannur', city: 'Kannur', total_screens: 4 },
  { id: 16, name: 'Gold Theater - Palakkad', location: 'GB Road, Palakkad', city: 'Palakkad', total_screens: 3 },
  { id: 17, name: 'Cinepolis - Kottayam', location: 'Meenachil Tower, Baker Junction', city: 'Kottayam', total_screens: 4 },
  { id: 18, name: 'PVR - Malappuram', location: 'Eureka Junction, Malappuram', city: 'Malappuram', total_screens: 4 },
  { id: 19, name: 'SRM Cinemas - Alappuzha', location: 'Mullakkal, Alappuzha', city: 'Alappuzha', total_screens: 3 },
  { id: 20, name: 'Miraj Cinemas - Kasaragod', location: 'City Mall, Kasaragod', city: 'Kasaragod', total_screens: 3 }
];

function getStoredMovies() {
  const raw = localStorage.getItem('th_offline_movies');
  if (raw) {
    try { return JSON.parse(raw); } catch (e) {}
  }
  localStorage.setItem('th_offline_movies', JSON.stringify(DEFAULT_SEED_MOVIES));
  return [...DEFAULT_SEED_MOVIES];
}

function saveStoredMovies(movies) {
  localStorage.setItem('th_offline_movies', JSON.stringify(movies));
}

// ── Offline Request Router ───────────────────
function handleOfflineRequest(path, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const [cleanPath, queryString] = path.split('?');
  const queryParams = new URLSearchParams(queryString || '');

  // 1. DELETE /movies/:id
  if (method === 'DELETE' && cleanPath.startsWith('/movies/')) {
    const id = Number(cleanPath.replace('/movies/', ''));
    let movies = getStoredMovies();
    const countBefore = movies.length;
    movies = movies.filter(m => Number(m.id) !== id);
    saveStoredMovies(movies);
    return { success: true, message: 'Movie deleted successfully.' };
  }

  // 2. GET /movies
  if (method === 'GET' && cleanPath === '/movies') {
    let movies = getStoredMovies();
    const lang = queryParams.get('language');
    const genre = queryParams.get('genre');
    const search = queryParams.get('search');
    if (lang)   movies = movies.filter(m => m.language.toLowerCase() === lang.toLowerCase());
    if (genre)  movies = movies.filter(m => m.genre.toLowerCase().includes(genre.toLowerCase()));
    if (search) movies = movies.filter(m => m.title.toLowerCase().includes(search.toLowerCase()));
    return movies;
  }

  // 3. GET /movies/:id
  if (method === 'GET' && cleanPath.match(/^\/movies\/\d+$/)) {
    const id = Number(cleanPath.replace('/movies/', ''));
    const movie = getStoredMovies().find(m => Number(m.id) === id);
    if (!movie) throw new Error('Movie not found');
    return movie;
  }

  // 4. POST /movies
  if (method === 'POST' && cleanPath === '/movies') {
    const body = JSON.parse(options.body || '{}');
    const movies = getStoredMovies();
    const newMovie = {
      id: Date.now(),
      title: body.title || 'Untitled',
      language: body.language || 'Malayalam',
      genre: body.genre || 'Drama',
      duration_min: Number(body.duration_min || body.durationMin || 130),
      rating: Number(body.rating || 8.0),
      poster_url: body.poster_url || body.posterUrl || 'https://image.tmdb.org/t/p/w500/bswrtewwthpsh6nABiqKevU4UBI.jpg',
      description: body.description || '',
      status: body.status || 'NOW_SHOWING'
    };
    movies.unshift(newMovie);
    saveStoredMovies(movies);
    return newMovie;
  }

  // 5. PUT /movies/:id
  if (method === 'PUT' && cleanPath.startsWith('/movies/')) {
    const id = Number(cleanPath.replace('/movies/', ''));
    const body = JSON.parse(options.body || '{}');
    const movies = getStoredMovies();
    const idx = movies.findIndex(m => Number(m.id) === id);
    if (idx !== -1) {
      movies[idx] = { ...movies[idx], ...body, id };
      saveStoredMovies(movies);
      return movies[idx];
    }
    throw new Error('Movie not found');
  }

  // 6. GET /movies/:id/shows
  if (method === 'GET' && cleanPath.includes('/shows')) {
    const today = new Date().toISOString().split('T')[0];
    return [
      { id: 101, show_date: today, show_time: '10:00:00', price_std: 220, price_prem: 380, screen_id: 1, screen_name: 'Audi 1 - 4K Dolby Atmos', total_seats: 100, seats_booked: 14, seats_avail: 86, theatre_id: 1, theatre_name: 'PVR Cinemas - LuLu Mall', city: 'Kochi', location: 'Edapally' },
      { id: 102, show_date: today, show_time: '14:30:00', price_std: 220, price_prem: 380, screen_id: 2, screen_name: 'Audi 2 - Laser 7.1', total_seats: 100, seats_booked: 28, seats_avail: 72, theatre_id: 1, theatre_name: 'PVR Cinemas - LuLu Mall', city: 'Kochi', location: 'Edapally' },
      { id: 103, show_date: today, show_time: '18:45:00', price_std: 250, price_prem: 400, screen_id: 3, screen_name: 'Audi 1 - Luxe Atmos', total_seats: 100, seats_booked: 45, seats_avail: 55, theatre_id: 6, theatre_name: 'INOX - Trivandrum', city: 'Thiruvananthapuram', location: 'Pattom' },
      { id: 104, show_date: today, show_time: '21:30:00', price_std: 250, price_prem: 400, screen_id: 4, screen_name: 'Screen 1 - 4K Laser', total_seats: 100, seats_booked: 19, seats_avail: 81, theatre_id: 10, theatre_name: 'PVR - Kozhikode', city: 'Kozhikode', location: 'HiLite Mall' }
    ];
  }

  // 7. GET /theatres
  if (method === 'GET' && cleanPath === '/theatres') {
    const city = queryParams.get('city');
    if (city) {
      return DEFAULT_SEED_THEATRES.filter(t => t.city.toLowerCase() === city.toLowerCase());
    }
    return DEFAULT_SEED_THEATRES;
  }

  // 8. GET /theatres/cities
  if (method === 'GET' && cleanPath === '/theatres/cities') {
    return ['Kochi', 'Thiruvananthapuram', 'Kozhikode', 'Thrissur', 'Kollam', 'Kannur', 'Palakkad', 'Kottayam', 'Malappuram', 'Alappuzha', 'Kasaragod'];
  }

  // 9. GET /theatres/:id
  if (method === 'GET' && cleanPath.match(/^\/theatres\/\d+$/)) {
    const id = Number(cleanPath.replace('/theatres/', ''));
    return DEFAULT_SEED_THEATRES.find(t => t.id === id) || DEFAULT_SEED_THEATRES[0];
  }

  // 10. GET /seats/:show_id
  if (method === 'GET' && cleanPath.startsWith('/seats/')) {
    return {
      show_id: 101,
      rows: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'],
      seats_per_row: 10,
      booked: ['A3', 'A4', 'C5', 'C6', 'D1', 'E8', 'E9'],
      prices: { standard: 220, premium: 380 }
    };
  }

  // 11. POST /bookings
  if (method === 'POST' && cleanPath === '/bookings') {
    const body = JSON.parse(options.body || '{}');
    const seats = body.seats || [];
    const ref = 'TH-' + Math.floor(100000 + Math.random() * 900000);
    const newBooking = {
      id: Date.now(),
      booking_ref: ref,
      bookingRef: ref,
      movie_title: 'Manjummel Boys',
      theatre_name: 'PVR Cinemas - LuLu Mall',
      screen_name: 'Audi 1 - 4K Dolby Atmos',
      show_date: new Date().toISOString().split('T')[0],
      show_time: '18:45:00',
      seats: seats.map(s => typeof s === 'string' ? s : (s.label || s)),
      total_amount: seats.length * 250,
      status: 'Confirmed',
      booked_at: new Date().toISOString()
    };
    const bookings = JSON.parse(localStorage.getItem('th_offline_bookings') || '[]');
    bookings.unshift(newBooking);
    localStorage.setItem('th_offline_bookings', JSON.stringify(bookings));
    return newBooking;
  }

  // 12. GET /bookings/mine
  if (method === 'GET' && cleanPath === '/bookings/mine') {
    return JSON.parse(localStorage.getItem('th_offline_bookings') || '[]');
  }

  // 13. GET /admin/stats
  if (method === 'GET' && cleanPath === '/admin/stats') {
    const movies = getStoredMovies();
    const bookings = JSON.parse(localStorage.getItem('th_offline_bookings') || '[]');
    return {
      total_movies: movies.length,
      total_theatres: 20,
      total_users: 142,
      total_bookings: 38 + bookings.length,
      total_revenue: 124500 + (bookings.length * 500)
    };
  }

  // 14. POST /auth/login
  if (method === 'POST' && cleanPath === '/auth/login') {
    const body = JSON.parse(options.body || '{}');
    const email = (body.email || '').toLowerCase().trim();
    const isAdmin = email.includes('admin');
    const user = {
      id: isAdmin ? 1 : 2,
      name: isAdmin ? 'Cinema Administrator' : 'Guest Cinephile',
      email: email || (isAdmin ? 'admin@tickethub.com' : 'user@tickethub.com'),
      role: isAdmin ? 'admin' : 'customer'
    };
    const token = 'th_offline_jwt_token_' + Date.now();
    Auth.setToken(token);
    Auth.setUser(user);
    return { token, user, id: user.id, name: user.name, email: user.email, role: user.role };
  }

  // 15. GET /auth/me
  if (method === 'GET' && cleanPath === '/auth/me') {
    return Auth.getUser() || { id: 2, name: 'Guest Cinephile', email: 'guest@tickethub.com', role: 'customer' };
  }

  return { message: 'OK' };
}

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
        return handleOfflineRequest(path, options);
      }
    } else {
      return handleOfflineRequest(path, options);
    }
  }

  if (!res.ok) {
    // If backend returns 404 or 500 for a movie delete or get, try offline fallback
    if (res.status === 404 || res.status === 500) {
      try {
        return handleOfflineRequest(path, options);
      } catch (e) {}
    }
    const data = await res.json().catch(() => ({}));
    const errorMsg = data.error || data.message || `Request failed (${res.status})`;
    throw new Error(errorMsg);
  }

  return await res.json().catch(() => ({}));
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
  me:             ()                             => apiFetch('/auth/me'),
  updateProfile:  (name, phone)                  => apiFetch('/auth/profile',  { method: 'PUT',  body: JSON.stringify({ name, phone }) }),
  changePassword: (currentPassword, newPassword) => apiFetch('/auth/password', { method: 'PUT', body: JSON.stringify({ currentPassword, newPassword }) }),
};

// ── MOVIES ────────────────────────────────────
const MoviesAPI = {
  getAll:   (params = {}) => apiFetch('/movies?' + new URLSearchParams(params)),
  getOne:   (id)          => apiFetch(`/movies/${id}`),
  getShows: (id, params)  => apiFetch(`/movies/${id}/shows?` + new URLSearchParams(params)),
  add:      (data)        => apiFetch('/movies',       { method: 'POST',   body: JSON.stringify(data) }),
  update:   (id, data)    => apiFetch(`/movies/${id}`, { method: 'PUT',    body: JSON.stringify(data) }),
  delete:   (id)          => apiFetch(`/movies/${id}`, { method: 'DELETE' }),
};

// ── THEATRES ──────────────────────────────────
const TheatresAPI = {
  getAll:    (params = {}) => apiFetch('/theatres?' + new URLSearchParams(params)),
  getCities: ()            => apiFetch('/theatres/cities'),
  getOne:    (id)          => apiFetch(`/theatres/${id}`),
  getShows:  (id)          => apiFetch(`/theatres/${id}/shows`),
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
  mine:    ()    => apiFetch('/bookings/mine'),
  getOne:  (ref) => apiFetch(`/bookings/${ref}`),
  cancel:  (ref) => apiFetch(`/bookings/${ref}/cancel`, { method: 'PUT' }),
  all:     ()    => apiFetch('/bookings'),
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
  deleteMovie: id => apiFetch(`/movies/${id}`, { method: 'DELETE' }),
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
      <a href="/login.html#register" class="btn btn-primary btn-sm">Sign Up</a>`;
  }
}

// ── Universal Helper Functions ────────────────
function fmtDuration(mins) {
  if (!mins) return '2h 15m';
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return `${h}h ${m}m`;
}

function genEmoji(genre) {
  if (!genre) return '🎬';
  const g = String(genre).toLowerCase();
  if (g.includes('action')) return '💥';
  if (g.includes('comedy')) return '😄';
  if (g.includes('horror')) return '👻';
  if (g.includes('romance')) return '❤️';
  if (g.includes('thriller') || g.includes('mystery')) return '🔍';
  if (g.includes('sci-fi')) return '🚀';
  if (g.includes('adventure')) return '🗺️';
  if (g.includes('drama') || g.includes('biography')) return '🎭';
  if (g.includes('war')) return '⚔️';
  return '🎬';
}

function posterImg(m) {
  const p = m.poster_url || m.posterUrl || m.posterPath;
  if (p) {
    return `<img src="${p}" alt="${m.title}" class="movie-poster-img" style="width:100%;height:100%;object-fit:cover;" onerror="this.onerror=null;this.src='/assets/tickethub_ticket_patch.png'"/>`;
  }
  return `<div class="poster-placeholder c1" style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;font-size:2rem;background:#1a1d28;">${genEmoji(m.genre)}</div>`;
}

function formatINR(amount) {
  return '₹' + Number(amount || 0).toLocaleString('en-IN');
}

function escapeQuotes(str) {
  if (!str) return '';
  return String(str).replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

function escapeQuote(str) {
  return escapeQuotes(str);
}

function initCinemaIntro() {
  const intro = document.getElementById('cinemaIntro');
  if (!intro) return;
  const skipBtn = document.getElementById('skipIntroBtn');
  const dismiss = () => {
    intro.classList.add('fade-out');
    setTimeout(() => {
      intro.style.display = 'none';
    }, 850);
  };
  if (skipBtn) skipBtn.addEventListener('click', dismiss);
  setTimeout(dismiss, 2400);
}

// Guarantee global availability in window scope
if (typeof window !== 'undefined') {
  window.fmtDuration = fmtDuration;
  window.genEmoji = genEmoji;
  window.posterImg = posterImg;
  window.formatINR = formatINR;
  window.escapeQuotes = escapeQuotes;
  window.escapeQuote = escapeQuote;
  window.initCinemaIntro = initCinemaIntro;
}

document.addEventListener('DOMContentLoaded', () => {
  const oldBanner = document.getElementById('thOfflineNotice');
  if (oldBanner) oldBanner.remove();
  updateNav();
});
