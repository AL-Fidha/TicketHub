package com.tickethub.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Show ID is required")
    @JsonProperty("show_id")
    @JsonAlias({"showId", "show_id"})
    private Long showId;

    @NotNull(message = "Seats selection is required")
    @NotEmpty(message = "Please select at least 1 seat to proceed")
    @Size(max = 8, message = "Maximum 8 seats permitted per booking")
    @Valid
    private List<SeatSelection> seats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatSelection {
        @NotBlank(message = "Seat label is required")
        private String label;   // e.g. A1, B3

        private String type;    // "standard" or "premium"
    }
}
