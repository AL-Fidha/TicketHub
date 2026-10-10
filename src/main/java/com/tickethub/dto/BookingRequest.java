package com.tickethub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull
    private Long showId;

    @NotEmpty @Size(max = 8, message = "Maximum 8 seats per booking")
    private List<SeatSelection> seats;

    @Data
    public static class SeatSelection {
        @NotBlank
        private String label;   // e.g. A1, B3
        @NotBlank
        private String type;    // "standard" or "premium"
    }
}
