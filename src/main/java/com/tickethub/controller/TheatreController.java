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

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return theatreRepo.findById(id)
            .<ResponseEntity<?>>map(theatre -> ResponseEntity.ok(toMap(theatre)))
            .orElse(ResponseEntity.status(404).body(Map.of("error", "Theatre not found.")));
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
