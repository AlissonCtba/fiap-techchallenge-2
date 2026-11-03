package com.restaurant.usertype.dto;

import jakarta.validation.constraints.NotBlank;

public record UserTypeRequest(@NotBlank String name) {}
