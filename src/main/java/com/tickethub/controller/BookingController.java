package com.tickethub.controller;

import com.tickethub.dto.BookingRequest;
import com.tickethub.model.User;
import com.tickethub.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody BookingRequest req) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Please log in to book tickets."));
        }
        try {
            return ResponseEntity.status(201).body(bookingService.createBooking(user.getId(), req));
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getMyBookings(user.getId()));
    }

    @GetMapping
    public ResponseEntity<?> all(@AuthenticationPrincipal User user) {
        if (user.getRole() != User.Role.admin)
            return ResponseEntity.status(403).body(Map.of("error", "Admin access required."));
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{ref}")
    public ResponseEntity<?> getOne(@PathVariable String ref,
                                    @AuthenticationPrincipal User user) {
        try {
            Map<String, Object> b = bookingService.getByRef(ref);
            // Allow owner or admin
            String email = (String) b.get("customer_email");
            if (!user.getEmail().equals(email) && user.getRole() != User.Role.admin)
                return ResponseEntity.status(403).body(Map.of("error", "Access denied."));
            return ResponseEntity.ok(b);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{ref}/cancel")
    public ResponseEntity<?> cancel(@PathVariable String ref,
                                    @AuthenticationPrincipal User user) {
        try {
            bookingService.cancel(ref, user.getId(), user.getRole() == User.Role.admin);
            return ResponseEntity.ok(Map.of("message", "Booking cancelled."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
