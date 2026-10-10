package com.tickethub.controller;

import com.tickethub.model.Movie;
import com.tickethub.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(movieService.getAll(language, genre, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(movieService.getOne(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/shows")
    public ResponseEntity<?> getShows(
            @PathVariable Long id,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long theatre_id) {
        return ResponseEntity.ok(movieService.getShows(id, date, theatre_id));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Movie movie) {
        try {
            return ResponseEntity.status(201).body(movieService.save(movie));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Movie movie) {
        try {
            return ResponseEntity.ok(movieService.update(id, movie));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Movie deleted."));
    }
}
