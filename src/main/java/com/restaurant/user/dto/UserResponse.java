package com.restaurant.user.dto;

import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.dto.UserTypeResponse;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String login,
        String email,
        UserRole role,
        UserTypeResponse userType,
        Address address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime passwordChangedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getRole(),
                UserTypeResponse.from(user.getUserType()),
                user.getAddress(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getPasswordChangedAt()
        );
    }
}
