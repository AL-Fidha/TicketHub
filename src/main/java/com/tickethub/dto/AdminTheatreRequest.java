package com.tickethub.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminTheatreRequest(
    @NotBlank String name,
    @NotBlank String location,
    @NotBlank String city,
    @NotNull @Min(1) Integer rows,
    @NotNull @Min(1) Integer cols
) {}