package com.restaurant.restaurant.dto;

import com.restaurant.restaurant.Restaurant;
import com.restaurant.user.Address;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record RestaurantResponse(
        Long id,
        String name,
        Address address,
        String cuisineType,
        LocalTime openingTime,
        LocalTime closingTime,
        Long ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(
                r.getId(),
                r.getName(),
                r.getAddress(),
                r.getCuisineType(),
                r.getOpeningTime(),
                r.getClosingTime(),
                r.getOwner().getId(),
                r.getOwner().getName(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
