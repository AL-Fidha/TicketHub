package com.tickethub.controller;

import com.tickethub.model.Show;
import com.tickethub.repository.BookingSeatRepository;
import com.tickethub.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowRepository showRepo;
    private final BookingSeatRepository bsRepo;

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return showRepo.findByIdWithDetails(id)
            .<ResponseEntity<?>>map(show -> ResponseEntity.ok(toMap(show)))
            .orElse(ResponseEntity.status(404).body(Map.of("error", "Show not found.")));
    }

    private Map<String, Object> toMap(Show s) {
        long booked = bsRepo.countBookedSeatsByShowId(s.getId());
        int total = s.getScreen().getTotalSeats();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("show_id", s.getId());
        map.put("id", s.getId());
        map.put("show_date", s.getShowDate().toString());
        map.put("show_time", s.getShowTime().toString());
        map.put("price_std", s.getPriceStd());
        map.put("price_prem", s.getPricePrem());
        map.put("screen_id", s.getScreen().getId());
        map.put("screen_name", s.getScreen().getName());
        map.put("total_seats", total);
        map.put("seats_booked", booked);
        map.put("seats_avail", total - booked);
        map.put("theatre_id", s.getScreen().getTheatre().getId());
        map.put("theatre_name", s.getScreen().getTheatre().getName());
        map.put("location", s.getScreen().getTheatre().getLocation());
        map.put("city", s.getScreen().getTheatre().getCity());
        map.put("movie_id", s.getMovie().getId());
        map.put("movie_title", s.getMovie().getTitle());
        return map;
    }
}
