package com.tickethub.service;

import com.tickethub.model.Show;
import com.tickethub.repository.BookingSeatRepository;
import com.tickethub.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final ShowRepository        showRepo;
    private final BookingSeatRepository bsRepo;

    public Map<String, Object> getSeatMap(Long showId) {
        Show show = showRepo.findById(showId)
            .orElseThrow(() -> new RuntimeException("Show not found."));

        List<String> booked = bsRepo.findBookedSeatsByShowId(showId);

        return Map.of(
            "rows",        show.getScreen().getRows(),
            "cols",        show.getScreen().getCols(),
            "total_seats", show.getScreen().getTotalSeats(),
            "price_std",   show.getPriceStd(),
            "price_prem",  show.getPricePrem(),
            "booked",      booked    // e.g. ["A1","B3","C5"]
        );
    }
}
