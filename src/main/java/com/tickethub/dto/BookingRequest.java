package com.tickethub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class BookingRequest {

    @NotNull
    @com.fasterxml.jackson.annotation.JsonAlias({"show_id", "showId"})
    private Long showId;

    @com.fasterxml.jackson.annotation.JsonProperty("show_id")
    public Long getShowIdSnake() {
        return showId;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("show_id")
    public void setShowIdSnake(Long showId) {
        this.showId = showId;
    }

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
