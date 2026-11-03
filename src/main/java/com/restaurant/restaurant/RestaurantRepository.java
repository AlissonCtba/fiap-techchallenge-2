package com.restaurant.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Restaurant> findByCuisineTypeContainingIgnoreCase(String cuisineType, Pageable pageable);
    boolean existsByNameAndIdNot(String name, Long id);
    Optional<Restaurant> findByIdAndOwnerEmail(Long id, String ownerEmail);
}
