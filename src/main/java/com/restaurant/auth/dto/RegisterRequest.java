package com.restaurant.auth.dto;

import com.restaurant.user.Address;
import com.restaurant.user.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank String login,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6) String password,
        @NotNull UserRole role,
        @Valid @NotNull Address address
) {}
