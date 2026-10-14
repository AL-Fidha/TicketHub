package com.tickethub.controller;

import com.tickethub.dto.AdminShowRequest;
import com.tickethub.dto.AdminTheatreRequest;
import com.tickethub.model.Movie;
import com.tickethub.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final com.tickethub.service.MovieService movieService;

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<?> users() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/theatres")
    public ResponseEntity<?> theatres() {
        return ResponseEntity.ok(adminService.getTheatres());
    }

    @PostMapping("/theatres")
    public ResponseEntity<?> createTheatre(@Valid @RequestBody AdminTheatreRequest request) {
        try {
            return ResponseEntity.status(201).body(adminService.createTheatre(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/screens")
    public ResponseEntity<?> screens() {
        return ResponseEntity.ok(adminService.getScreens());
    }

    @GetMapping("/shows")
    public ResponseEntity<?> shows() {
        return ResponseEntity.ok(adminService.getShows());
    }

    @PostMapping("/shows")
    public ResponseEntity<?> createShow(@Valid @RequestBody AdminShowRequest request) {
        try {
            return ResponseEntity.status(201).body(adminService.createShow(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/shows/broadcast")
    public ResponseEntity<?> broadcastShow(@Valid @RequestBody com.tickethub.dto.AdminBroadcastShowRequest request) {
        try {
            return ResponseEntity.status(201).body(adminService.broadcastShowToAllTheatres(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/theatre-lineup")
    public ResponseEntity<?> theatreLineup() {
        return ResponseEntity.ok(adminService.getTheatreLineup());
    }

    @DeleteMapping("/shows/{id}")
    public ResponseEntity<?> deleteShow(@PathVariable Long id) {
        try {
            adminService.deleteShow(id);
            return ResponseEntity.ok(Map.of("message", "Show deleted."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        try {
            movieService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Movie deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unable to delete movie: " + e.getMessage()));
        }
    }

    @GetMapping("/tmdb/movies")
    public ResponseEntity<?> discoverTmdbMovies(@RequestParam Movie.Language language) {
        try {
            return ResponseEntity.ok(adminService.discoverTmdbMovies(language));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tmdb/movies/{tmdbId}")
    public ResponseEntity<?> importTmdbMovie(
            @PathVariable Long tmdbId,
            @RequestParam Movie.Language language) {
        try {
            return ResponseEntity.status(201).body(adminService.importTmdbMovie(tmdbId, language));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
