package com.restaurant.menu;

import com.restaurant.menu.dto.MenuItemRequest;
import com.restaurant.menu.dto.MenuItemResponse;
import com.restaurant.restaurant.Restaurant;
import com.restaurant.restaurant.RestaurantNotFoundException;
import com.restaurant.restaurant.RestaurantRepository;
import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock MenuItemRepository menuItemRepository;
    @Mock RestaurantRepository restaurantRepository;
    @InjectMocks MenuItemService menuItemService;

    private User owner;
    private Restaurant restaurant;
    private MenuItem menuItem;
    private MenuItemRequest request;

    @BeforeEach
    void setUp() {
        Address address = new Address("Rua A", "1", null, "SP", "SP", "01310-000");
        owner = User.builder().id(1L).email("owner@test.com").role(UserRole.OWNER)
                .userType(new UserType(1L, "OWNER")).address(address).name("Owner").build();
        restaurant = Restaurant.builder().id(1L).name("Cantina").address(address)
                .cuisineType("Italiana").openingTime(LocalTime.of(11, 0))
                .closingTime(LocalTime.of(23, 0)).owner(owner).build();
        request = new MenuItemRequest("Lasanha", "Deliciosa", BigDecimal.valueOf(45.90), false, "/img/lasanha.jpg");
        menuItem = MenuItem.builder().id(1L).name("Lasanha").description("Deliciosa")
                .price(BigDecimal.valueOf(45.90)).dineInOnly(false)
                .photoPath("/img/lasanha.jpg").restaurant(restaurant).build();
    }

    @Test
    void create_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.save(any())).thenAnswer(inv -> {
            MenuItem item = inv.getArgument(0);
            item.setId(1L);
            return item;
        });

        MenuItemResponse response = menuItemService.create("owner@test.com", 1L, request);

        assertThat(response.name()).isEqualTo("Lasanha");
        assertThat(response.restaurantName()).isEqualTo("Cantina");
    }

    @Test
    void create_notOwner_throws() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> menuItemService.create("outro@test.com", 1L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_restaurantNotFound_throws() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuItemService.create("owner@test.com", 99L, request))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void findByRestaurant_success() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        when(menuItemRepository.findByRestaurantId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(menuItem)));

        Page<MenuItemResponse> result = menuItemService.findByRestaurant(1L, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByRestaurant_restaurantNotFound_throws() {
        when(restaurantRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> menuItemService.findByRestaurant(99L, PageRequest.of(0, 10)))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void findById_success() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        MenuItemResponse response = menuItemService.findById(1L, 1L);

        assertThat(response.name()).isEqualTo("Lasanha");
    }

    @Test
    void findById_itemNotFound_throws() {
        when(restaurantRepository.existsById(1L)).thenReturn(true);
        when(menuItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuItemService.findById(1L, 99L))
                .isInstanceOf(MenuItemNotFoundException.class);
    }

    @Test
    void update_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MenuItemResponse response = menuItemService.update("owner@test.com", 1L, 1L,
                new MenuItemRequest("Lasanha Premium", "Melhor", BigDecimal.valueOf(59.90), true, "/img/premium.jpg"));

        assertThat(response.name()).isEqualTo("Lasanha Premium");
        assertThat(response.price()).isEqualByComparingTo("59.90");
    }

    @Test
    void delete_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));

        menuItemService.delete("owner@test.com", 1L, 1L);

        verify(menuItemRepository).delete(menuItem);
    }

    @Test
    void delete_notOwner_throws() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> menuItemService.delete("outro@test.com", 1L, 1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(menuItemRepository, never()).delete(any());
    }
}
