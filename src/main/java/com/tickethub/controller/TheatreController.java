package com.tickethub.controller;

import com.tickethub.model.Theatre;
import com.tickethub.repository.TheatreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/theatres")
@RequiredArgsConstructor
public class TheatreController {

    private final TheatreRepository theatreRepo;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) String city) {
        List<Theatre> theatres = city != null ? theatreRepo.findByCity(city) : theatreRepo.findAll();
        return ResponseEntity.ok(theatres.stream().map(this::toMap).toList());
    }

    @GetMapping("/cities")
    public ResponseEntity<?> getCities() {
        return ResponseEntity.ok(theatreRepo.findAllCities());
    }

    private final com.tickethub.repository.ShowRepository showRepo;

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return theatreRepo.findById(id)
            .<ResponseEntity<?>>map(theatre -> ResponseEntity.ok(toMap(theatre)))
            .orElse(ResponseEntity.status(404).body(Map.of("error", "Theatre not found.")));
    }

    @GetMapping("/{id}/shows")
    public ResponseEntity<?> getShowsByTheatre(@PathVariable Long id) {
        var shows = showRepo.findByTheatreId(id);
        var list = shows.stream().map(s -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("show_id", s.getId());
            map.put("show_date", s.getShowDate().toString());
            map.put("show_time", s.getShowTime().toString());
            map.put("price_std", s.getPriceStd());
            map.put("price_prem", s.getPricePrem());
            map.put("screen_name", s.getScreen().getName());
            map.put("movie_id", s.getMovie().getId());
            map.put("movie_title", s.getMovie().getTitle());
            map.put("movie_genre", s.getMovie().getGenre());
            map.put("movie_language", s.getMovie().getLanguage().name());
            map.put("movie_rating", s.getMovie().getRating());
            map.put("poster_url", s.getMovie().getPosterUrl());
            map.put("duration_min", s.getMovie().getDurationMin());
            return map;
        }).toList();
        return ResponseEntity.ok(list);
    }

    private Map<String, Object> toMap(Theatre theatre) {
        return Map.of(
            "id", theatre.getId(),
            "name", theatre.getName(),
            "location", theatre.getLocation(),
            "city", theatre.getCity()
        );
    }
}
