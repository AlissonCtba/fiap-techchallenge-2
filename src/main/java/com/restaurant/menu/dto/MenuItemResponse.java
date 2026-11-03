package com.restaurant.menu.dto;

import com.restaurant.menu.MenuItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean dineInOnly,
        String photoPath,
        Long restaurantId,
        String restaurantName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.isDineInOnly(),
                item.getPhotoPath(),
                item.getRestaurant().getId(),
                item.getRestaurant().getName(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
