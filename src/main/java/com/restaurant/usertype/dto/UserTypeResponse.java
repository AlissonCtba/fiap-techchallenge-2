package com.restaurant.usertype.dto;

import com.restaurant.usertype.UserType;

public record UserTypeResponse(Long id, String name) {
    public static UserTypeResponse from(UserType type) {
        return new UserTypeResponse(type.getId(), type.getName());
    }
}
