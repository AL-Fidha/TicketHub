package com.tickethub.service;

import com.tickethub.dto.AdminShowRequest;
import com.tickethub.dto.AdminTheatreRequest;
import com.tickethub.model.*;
import com.tickethub.repository.*;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MovieRepository   movieRepo;
    private final TheatreRepository theatreRepo;
    private final UserRepository    userRepo;
    private final BookingRepository bookingRepo;
    private final ScreenRepository  screenRepo;
    private final ShowRepository    showRepo;
    private final TmdbService       tmdbService;

    public Map<String, Object> getStats() {
        long totalMovies   = movieRepo.count();
        long totalTheatres = theatreRepo.count();
        long totalUsers    = userRepo.findAll().stream()
            .filter(u -> u.getRole() == User.Role.customer).count();
        long totalBookings = bookingRepo.countByStatus(com.tickethub.model.Booking.Status.Confirmed);
        double revenue     = bookingRepo.getTotalRevenue() != null
            ? bookingRepo.getTotalRevenue() : 0.0;

        return Map.of(
            "total_movies",   totalMovies,
            "total_theatres", totalTheatres,
            "total_users",    totalUsers,
            "total_bookings", totalBookings,
            "total_revenue",  revenue
        );
    }

    public List<Map<String, Object>> getAllUsers() {
        return userRepo.findAll().stream().map(u -> Map.<String, Object>of(
            "id",         u.getId(),
            "name",       u.getName(),
            "email",      u.getEmail(),
            "phone",      u.getPhone() != null ? u.getPhone() : "",
            "role",       u.getRole().name(),
            "created_at", u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTheatres() {
        return theatreRepo.findAll().stream().map(theatre -> Map.<String, Object>of(
            "id", theatre.getId(),
            "name", theatre.getName(),
            "location", theatre.getLocation(),
            "city", theatre.getCity()
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getScreens() {
        return screenRepo.findAll().stream().map(screen -> Map.<String, Object>of(
            "id", screen.getId(),
            "name", screen.getName(),
            "theatre_id", screen.getTheatre().getId(),
            "theatre_name", screen.getTheatre().getName(),
            "city", screen.getTheatre().getCity(),
            "total_seats", screen.getTotalSeats()
        )).toList();
    }

    @Transactional
    public Map<String, Object> createTheatre(AdminTheatreRequest request) {
        int seatCount = Math.multiplyExact(request.rows(), request.cols());
        if (seatCount > 500) throw new IllegalArgumentException("A screen cannot exceed 500 seats.");

        Theatre theatre = theatreRepo.save(Theatre.builder()
            .name(request.name().trim())
            .location(request.location().trim())
            .city(request.city().trim())
            .build());
        Screen screen = screenRepo.save(Screen.builder()
            .theatre(theatre)
            .name("Screen 1")
            .totalSeats(seatCount)
            .rows(request.rows())
            .cols(request.cols())
            .build());

        return Map.of("id", theatre.getId(), "screen_id", screen.getId(), "name", theatre.getName());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getShows() {
        return showRepo.findAllWithDetails().stream().map(this::showMap).toList();
    }

    @Transactional
    public Map<String, Object> createShow(AdminShowRequest request) {
        if (request.showDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Show date cannot be in the past.");
        if (showRepo.existsByScreen_IdAndShowDateAndShowTime(
                request.screenId(), request.showDate(), request.showTime()))
            throw new IllegalArgumentException("A show already exists on this screen at that time.");

        Movie movie = movieRepo.findById(request.movieId())
            .orElseThrow(() -> new IllegalArgumentException("Movie not found."));
        Screen screen = screenRepo.findById(request.screenId())
            .orElseThrow(() -> new IllegalArgumentException("Screen not found."));
        Show show = showRepo.save(Show.builder()
            .movie(movie)
            .screen(screen)
            .showDate(request.showDate())
            .showTime(request.showTime())
            .priceStd(request.priceStd())
            .pricePrem(request.pricePrem())
            .build());
        return showMap(show);
    }

    @Transactional
    public Map<String, Object> broadcastShowToAllTheatres(com.tickethub.dto.AdminBroadcastShowRequest request) {
        if (request.showDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Show date cannot be in the past.");
        Movie movie = movieRepo.findById(request.movieId())
            .orElseThrow(() -> new IllegalArgumentException("Movie not found."));
        int created = 0;
        int skipped = 0;
        for (Theatre theatre : theatreRepo.findAll()) {
            java.util.List<Screen> screens = screenRepo.findByTheatreId(theatre.getId());
            if (screens.isEmpty()) { skipped++; continue; }
            Screen screen = screens.get(0);
            if (showRepo.existsByScreen_IdAndShowDateAndShowTime(screen.getId(), request.showDate(), request.showTime())) {
                skipped++;
                continue;
            }
            showRepo.save(Show.builder()
                .movie(movie)
                .screen(screen)
                .showDate(request.showDate())
                .showTime(request.showTime())
                .priceStd(request.priceStd())
                .pricePrem(request.pricePrem())
                .build());
            created++;
        }
        return Map.of("created", created, "skipped", skipped, "movie", movie.getTitle());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTheatreLineup() {
        List<Map<String, Object>> lineup = new java.util.ArrayList<>();
        for (Theatre theatre : theatreRepo.findAll()) {
            var shows = showRepo.findByTheatreId(theatre.getId());
            java.util.LinkedHashMap<Long, Map<String, Object>> movies = new java.util.LinkedHashMap<>();
            for (Show show : shows) {
                movies.computeIfAbsent(show.getMovie().getId(), id -> {
                    Map<String, Object> movie = new java.util.LinkedHashMap<>();
                    movie.put("id", show.getMovie().getId());
                    movie.put("title", show.getMovie().getTitle());
                    movie.put("language", show.getMovie().getLanguage().name());
                    movie.put("genre", show.getMovie().getGenre());
                    movie.put("cast", show.getMovie().getCast());
                    movie.put("producers", show.getMovie().getProducers());
                    movie.put("poster_url", show.getMovie().getPosterUrl());
                    movie.put("showtimes", new java.util.ArrayList<String>());
                    return movie;
                });
                @SuppressWarnings("unchecked")
                java.util.List<String> times = (java.util.List<String>) movies.get(show.getMovie().getId()).get("showtimes");
                times.add(show.getShowDate() + " " + show.getShowTime().toString().substring(0, 5));
            }
            lineup.add(Map.of(
                "id", theatre.getId(),
                "name", theatre.getName(),
                "city", theatre.getCity(),
                "location", theatre.getLocation(),
                "movies", movies.values()
            ));
        }
        return lineup;
    }

    @Transactional
    public void deleteShow(Long showId) {
        Show show = showRepo.findById(showId)
            .orElseThrow(() -> new IllegalArgumentException("Show not found."));
        if (bookingRepo.existsByShow_Id(showId))
            throw new IllegalArgumentException("Shows with booking history cannot be deleted.");
        showRepo.delete(show);
    }

    public List<Map<String, Object>> discoverTmdbMovies(Movie.Language language) {
        return tmdbService.discoverMovies(language);
    }

    @Transactional
    public Map<String, Object> importTmdbMovie(Long tmdbId, Movie.Language language) {
        if (movieRepo.findFirstByTmdbId(tmdbId).isPresent())
            throw new IllegalArgumentException("This TMDB movie is already in your catalogue.");

        JsonNode details = tmdbService.getMovieDetails(tmdbId);
        String title = details.path("title").asText("").trim();
        if (title.isBlank()) throw new IllegalArgumentException("TMDB did not return a movie title.");

        List<String> genres = new java.util.ArrayList<>();
        details.path("genres").forEach(genre -> {
            String name = genre.path("name").asText("");
            if (!name.isBlank()) genres.add(name);
        });
        int duration = details.path("runtime").asInt(0);
        String releaseDate = details.path("release_date").asText("");
        Movie.Status status = Movie.Status.NOW_SHOWING;
        if (!releaseDate.isBlank() && LocalDate.parse(releaseDate).isAfter(LocalDate.now()))
            status = Movie.Status.COMING_SOON;

        java.util.List<String> castNames = new java.util.ArrayList<>();
        details.path("credits").path("cast").forEach(person -> {
            String name = person.path("name").asText("");
            if (!name.isBlank() && castNames.size() < 6) castNames.add(name);
        });
        java.util.List<String> crewNames = new java.util.ArrayList<>();
        details.path("credits").path("crew").forEach(person -> {
            String job = person.path("job").asText("");
            if (("Director".equals(job) || "Producer".equals(job) || "Executive Producer".equals(job)) && crewNames.size() < 5) {
                String name = person.path("name").asText("");
                if (!name.isBlank() && !crewNames.contains(name)) crewNames.add(name);
            }
        });

        Movie movie = movieRepo.save(Movie.builder()
            .title(title)
            .genre(genres.isEmpty() ? "Drama" : String.join(", ", genres))
            .language(language)
            .durationMin(duration > 0 ? duration : 120)
            .rating(details.path("vote_average").asDouble(0))
            .description(details.path("overview").asText(""))
            .tmdbId(tmdbId)
            .posterPath(details.path("poster_path").asText(null))
            .cast(castNames.isEmpty() ? "Ensemble Cast" : String.join(", ", castNames))
            .producers(crewNames.isEmpty() ? "TicketHub Premiere Studios" : String.join(", ", crewNames))
            .status(status)
            .build());

        return Map.of(
            "id", movie.getId(),
            "title", movie.getTitle(),
            "poster_url", tmdbService.buildUrl(movie.getPosterPath()) == null ? "" : tmdbService.buildUrl(movie.getPosterPath()),
            "release_date", releaseDate
        );
    }

    private Map<String, Object> showMap(Show show) {
        return Map.ofEntries(
            Map.entry("id", show.getId()),
            Map.entry("movie_id", show.getMovie().getId()),
            Map.entry("movie", show.getMovie().getTitle()),
            Map.entry("screen_id", show.getScreen().getId()),
            Map.entry("screen", show.getScreen().getName()),
            Map.entry("theatre", show.getScreen().getTheatre().getName()),
            Map.entry("city", show.getScreen().getTheatre().getCity()),
            Map.entry("show_date", show.getShowDate().toString()),
            Map.entry("show_time", show.getShowTime().toString()),
            Map.entry("price_std", show.getPriceStd()),
            Map.entry("price_prem", show.getPricePrem())
        );
    }
}
