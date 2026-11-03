package com.restaurant.auth.dto;

import com.restaurant.user.Address;
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
        /** ID do tipo de usuário (ex: 1 = OWNER, 2 = CUSTOMER) */
        @NotNull Long userTypeId,
        @Valid @NotNull Address address
) {}
