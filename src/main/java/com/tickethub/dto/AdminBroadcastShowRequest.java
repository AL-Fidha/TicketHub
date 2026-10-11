package com.tickethub.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AdminBroadcastShowRequest(
    @NotNull @com.fasterxml.jackson.annotation.JsonAlias({"movieId", "movie_id"}) Long movieId,
    @NotNull @com.fasterxml.jackson.annotation.JsonAlias({"showDate", "show_date"}) LocalDate showDate,
    @NotNull @com.fasterxml.jackson.annotation.JsonAlias({"showTime", "show_time"}) LocalTime showTime,
    @NotNull @DecimalMin("0.01") @com.fasterxml.jackson.annotation.JsonAlias({"priceStd", "price_std"}) BigDecimal priceStd,
    @NotNull @DecimalMin("0.01") @com.fasterxml.jackson.annotation.JsonAlias({"pricePrem", "price_prem"}) BigDecimal pricePrem
) {}
