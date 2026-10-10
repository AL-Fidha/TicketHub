package com.tickethub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AdminShowRequest(
    @NotNull Long movieId,
    @NotNull Long screenId,
    @NotNull LocalDate showDate,
    @NotNull LocalTime showTime,
    @NotNull @DecimalMin("0.01") BigDecimal priceStd,
    @NotNull @DecimalMin("0.01") BigDecimal pricePrem
) {}