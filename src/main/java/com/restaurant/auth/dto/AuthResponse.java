package com.restaurant.auth.dto;

import com.restaurant.user.UserRole;

public record AuthResponse(
        String token,
        String email,
        String name,
        UserRole role
) {}
