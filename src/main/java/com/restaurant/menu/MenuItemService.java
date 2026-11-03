package com.restaurant.menu;

import com.restaurant.menu.dto.MenuItemRequest;
import com.restaurant.menu.dto.MenuItemResponse;
import com.restaurant.restaurant.Restaurant;
import com.restaurant.restaurant.RestaurantNotFoundException;
import com.restaurant.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public MenuItemResponse create(String ownerEmail, Long restaurantId, MenuItemRequest request) {
        Restaurant restaurant = getRestaurantForOwner(ownerEmail, restaurantId);

        MenuItem item = MenuItem.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .dineInOnly(request.dineInOnly())
                .photoPath(request.photoPath())
                .restaurant(restaurant)
                .build();

        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> findByRestaurant(Long restaurantId, Pageable pageable) {
        assertRestaurantExists(restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId, pageable)
                .map(MenuItemResponse::from);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse findById(Long restaurantId, Long itemId) {
        assertRestaurantExists(restaurantId);
        MenuItem item = getItem(itemId);
        assertBelongsToRestaurant(item, restaurantId);
        return MenuItemResponse.from(item);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> searchByName(Long restaurantId, String name, Pageable pageable) {
        assertRestaurantExists(restaurantId);
        return menuItemRepository
                .findByRestaurantIdAndNameContainingIgnoreCase(restaurantId, name, pageable)
                .map(MenuItemResponse::from);
    }

    @Transactional
    public MenuItemResponse update(String ownerEmail, Long restaurantId, Long itemId, MenuItemRequest request) {
        getRestaurantForOwner(ownerEmail, restaurantId);
        MenuItem item = getItem(itemId);
        assertBelongsToRestaurant(item, restaurantId);

        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setDineInOnly(request.dineInOnly());
        item.setPhotoPath(request.photoPath());

        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    @Transactional
    public void delete(String ownerEmail, Long restaurantId, Long itemId) {
        getRestaurantForOwner(ownerEmail, restaurantId);
        MenuItem item = getItem(itemId);
        assertBelongsToRestaurant(item, restaurantId);
        menuItemRepository.delete(item);
    }

    // helpers

    private Restaurant getRestaurantForOwner(String ownerEmail, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurante não encontrado: " + restaurantId));
        if (!restaurant.getOwner().getEmail().equals(ownerEmail)) {
            throw new AccessDeniedException("Você não é o dono deste restaurante");
        }
        return restaurant;
    }

    private MenuItem getItem(Long itemId) {
        return menuItemRepository.findById(itemId)
                .orElseThrow(() -> new MenuItemNotFoundException("Item não encontrado: " + itemId));
    }

    private void assertRestaurantExists(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException("Restaurante não encontrado: " + restaurantId);
        }
    }

    private void assertBelongsToRestaurant(MenuItem item, Long restaurantId) {
        if (!item.getRestaurant().getId().equals(restaurantId)) {
            throw new MenuItemNotFoundException("Item não pertence ao restaurante informado");
        }
    }
}
