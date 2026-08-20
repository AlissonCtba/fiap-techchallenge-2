package com.restaurant.user.dto;

import com.restaurant.user.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank String name,
        @NotBlank String login,
        @Email @NotBlank String email,
        @Valid @NotNull Address address
) {}
