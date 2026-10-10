package com.tickethub.service;

import com.tickethub.dto.BookingRequest;
import com.tickethub.model.*;
import com.tickethub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository     bookingRepo;
    private final BookingSeatRepository bsRepo;
    private final ShowRepository        showRepo;
    private final UserRepository        userRepo;
    private final TmdbService           tmdb;

    @Transactional
    public Map<String, Object> createBooking(Long userId, BookingRequest req) {
        if (req.getSeats().size() > 8)
            throw new RuntimeException("Maximum 8 seats per booking.");

        Show show = showRepo.findById(req.getShowId())
            .orElseThrow(() -> new RuntimeException("Show not found."));

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found."));

        // Check for conflicts
        List<String> requestedLabels = req.getSeats().stream()
            .map(BookingRequest.SeatSelection::getLabel).toList();

        List<String> alreadyBooked = bsRepo.findBookedSeatsByShowId(show.getId());
        List<String> conflicts = requestedLabels.stream()
            .filter(alreadyBooked::contains).toList();

        if (!conflicts.isEmpty())
            throw new RuntimeException("Seats already booked: " + String.join(", ", conflicts));

        // Calculate total
        BigDecimal total = req.getSeats().stream()
            .map(s -> "premium".equalsIgnoreCase(s.getType()) ? show.getPricePrem() : show.getPriceStd())
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(BigDecimal.valueOf(30)); // convenience fee

        // Generate booking reference
        String ref = "TH-" + Long.toString(Instant.now().toEpochMilli(), 36).toUpperCase()
                   + "-" + UUID.randomUUID().toString().substring(0,4).toUpperCase();

        Booking booking = Booking.builder()
            .bookingRef(ref)
            .user(user)
            .show(show)
            .totalAmount(total)
            .status(Booking.Status.Confirmed)
            .build();

        bookingRepo.save(booking);

        for (BookingRequest.SeatSelection s : req.getSeats()) {
            boolean isPrem = "premium".equalsIgnoreCase(s.getType());
            BookingSeat bs = BookingSeat.builder()
                .booking(booking)
                .show(show)
                .seatLabel(s.getLabel())
                .seatType(isPrem ? BookingSeat.SeatType.premium : BookingSeat.SeatType.standard)
                .price(isPrem ? show.getPricePrem() : show.getPriceStd())
                .build();
            bsRepo.save(bs);
        }

        return Map.of("booking_ref", ref, "total", total);
    }

    public List<Map<String, Object>> getMyBookings(Long userId) {
        return bookingRepo.findByUserId(userId).stream()
            .map(this::toMap)
            .toList();
    }

    public Map<String, Object> getByRef(String ref) {
        Booking b = bookingRepo.findByBookingRef(ref)
            .orElseThrow(() -> new RuntimeException("Booking not found."));
        return toMap(b);
    }

    @Transactional
    public void cancel(String ref, Long requestingUserId, boolean isAdmin) {
        Booking b = bookingRepo.findByBookingRef(ref)
            .orElseThrow(() -> new RuntimeException("Booking not found."));

        if (!isAdmin && !b.getUser().getId().equals(requestingUserId))
            throw new RuntimeException("Access denied.");

        if (b.getStatus() == Booking.Status.Cancelled)
            throw new RuntimeException("Booking is already cancelled.");

        b.setStatus(Booking.Status.Cancelled);
        bookingRepo.save(b);
    }

    public List<Map<String, Object>> getAllBookings() {
        return bookingRepo.findAllWithDetails().stream()
            .map(this::toMap)
            .toList();
    }

    private Map<String, Object> toMap(Booking b) {
        Show    show    = b.getShow();
        Movie   movie   = show.getMovie();
        Screen  screen  = show.getScreen();
        Theatre theatre = screen.getTheatre();

        List<String> seats = b.getSeats() == null ? List.of()
            : b.getSeats().stream().map(BookingSeat::getSeatLabel).sorted().toList();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",            b.getId());
        map.put("booking_ref",   b.getBookingRef());
        map.put("status",        b.getStatus().name());
        map.put("total_amount",  b.getTotalAmount());
        map.put("booked_at",     b.getBookedAt() != null ? b.getBookedAt().toString() : null);
        map.put("movie",         movie.getTitle());
        map.put("language",      movie.getLanguage().name());
        map.put("genre",         movie.getGenre());
        map.put("poster_url",    tmdb.buildUrl(movie.getPosterPath()));
        map.put("show_date",     show.getShowDate().toString());
        map.put("show_time",     show.getShowTime().toString());
        map.put("screen_name",   screen.getName());
        map.put("theatre_name",  theatre.getName());
        map.put("city",          theatre.getCity());
        map.put("seats",         seats);
        // include user info for admin view
        if (b.getUser() != null) {
            map.put("customer_name",  b.getUser().getName());
            map.put("customer_email", b.getUser().getEmail());
        }
        return map;
    }
}
