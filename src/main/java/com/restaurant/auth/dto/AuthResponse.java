package com.restaurant.auth.dto;

import com.restaurant.user.UserRole;
import com.restaurant.usertype.UserType;
import com.restaurant.usertype.dto.UserTypeResponse;

public record AuthResponse(
        String token,
        String email,
        String name,
        UserRole role,
        UserTypeResponse userType
) {
    public AuthResponse(String token, String email, String name, UserRole role, UserType userType) {
        this(token, email, name, role, UserTypeResponse.from(userType));
    }
}
