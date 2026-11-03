package com.restaurant.restaurant.dto;

import com.restaurant.user.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RestaurantRequest(
        @NotBlank String name,
        @Valid @NotNull Address address,
        @NotBlank String cuisineType,
        @NotNull LocalTime openingTime,
        @NotNull LocalTime closingTime
) {}
