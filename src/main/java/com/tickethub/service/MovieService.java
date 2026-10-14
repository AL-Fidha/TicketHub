package com.tickethub.service;

import com.tickethub.model.Movie;
import com.tickethub.model.Show;
import com.tickethub.repository.BookingSeatRepository;
import com.tickethub.repository.MovieRepository;
import com.tickethub.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository      movieRepo;
    private final ShowRepository       showRepo;
    private final BookingSeatRepository bsRepo;
    private final TmdbService          tmdb;
    private final EntityManager        em;

    public List<Movie> getAll(String language, String genre, String search) {
        Movie.Language lang = null;
        if (language != null && !language.isBlank()) {
            try { lang = Movie.Language.valueOf(language); } catch (Exception ignored) {}
        }
        List<Movie> movies = movieRepo.findFiltered(lang, genre, search);
        movies.forEach(this::attachPoster);
        return movies;
    }

    public Movie getOne(Long id) {
        Movie m = movieRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found."));
        attachPoster(m);
        return m;
    }

    public List<Map<String, Object>> getShows(Long movieId, String date, Long theatreId) {
        LocalDate localDate = (date != null && !date.isBlank()) ? LocalDate.parse(date) : null;
        List<Show> shows = showRepo.findByMovieFiltered(movieId, localDate, theatreId);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        return shows.stream()
            .filter(s -> {
                if (s.getShowDate().isBefore(today)) return false;
                if (s.getShowDate().isEqual(today) && s.getShowTime().isBefore(now)) return false;
                return true;
            })
            .map(s -> {
            long booked = bsRepo.countBookedSeatsByShowId(s.getId());
            int total   = s.getScreen().getTotalSeats();
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("show_id",       s.getId());
            map.put("show_date",     s.getShowDate().toString());
            map.put("show_time",     s.getShowTime().toString());
            map.put("price_std",     s.getPriceStd());
            map.put("price_prem",    s.getPricePrem());
            map.put("screen_id",     s.getScreen().getId());
            map.put("screen_name",   s.getScreen().getName());
            map.put("total_seats",   total);
            map.put("seats_booked",  booked);
            map.put("seats_avail",   total - booked);
            map.put("theatre_id",    s.getScreen().getTheatre().getId());
            map.put("theatre_name",  s.getScreen().getTheatre().getName());
            map.put("location",      s.getScreen().getTheatre().getLocation());
            map.put("city",          s.getScreen().getTheatre().getCity());
            return map;
        }).toList();
    }

    public Movie save(Movie movie) {
        // Fetch poster from TMDB if missing
        if (movie.getPosterPath() == null) {
            String path = tmdb.getPosterPath(movie.getTitle(), movie.getTmdbId());
            movie.setPosterPath(path);
        }
        Movie saved = movieRepo.save(movie);
        attachPoster(saved);
        return saved;
    }

    public Movie update(Long id, Movie updated) {
        Movie existing = movieRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Movie not found."));
        existing.setTitle(updated.getTitle());
        existing.setGenre(updated.getGenre());
        existing.setLanguage(updated.getLanguage());
        existing.setDurationMin(updated.getDurationMin());
        existing.setRating(updated.getRating());
        existing.setDescription(updated.getDescription());
        existing.setCast(updated.getCast());
        existing.setProducers(updated.getProducers());
        existing.setStatus(updated.getStatus());
        return movieRepo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!movieRepo.existsById(id)) {
            throw new IllegalArgumentException("Movie not found with id: " + id);
        }
        try {
            // 1. Delete booking_seats for all shows belonging to this movie
            em.createNativeQuery(
                "DELETE FROM booking_seats WHERE show_id IN (SELECT id FROM shows WHERE movie_id = :movieId)"
            ).setParameter("movieId", id).executeUpdate();

            // 2. Delete bookings for all shows belonging to this movie
            em.createNativeQuery(
                "DELETE FROM bookings WHERE show_id IN (SELECT id FROM shows WHERE movie_id = :movieId)"
            ).setParameter("movieId", id).executeUpdate();

            // 3. Delete shows belonging to this movie
            em.createNativeQuery(
                "DELETE FROM shows WHERE movie_id = :movieId"
            ).setParameter("movieId", id).executeUpdate();

            // 4. Finally delete the movie
            movieRepo.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete movie: " + e.getMessage(), e);
        }
    }

    private void attachPoster(Movie m) {
        if (m.getPosterPath() != null) {
            m.setPosterUrl(tmdb.buildUrl(m.getPosterPath()));
        } else {
            // Try to fetch and cache
            String path = tmdb.getPosterPath(m.getTitle(), m.getTmdbId());
            if (path != null) {
                m.setPosterPath(path);
                movieRepo.save(m);
                m.setPosterUrl(tmdb.buildUrl(path));
            }
        }
    }
}
