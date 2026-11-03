package com.restaurant.restaurant;

import com.restaurant.exception.UserNotFoundException;
import com.restaurant.restaurant.dto.RestaurantRequest;
import com.restaurant.restaurant.dto.RestaurantResponse;
import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
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

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock RestaurantRepository restaurantRepository;
    @Mock UserRepository userRepository;
    @InjectMocks RestaurantService restaurantService;

    private User owner;
    private User customer;
    private Restaurant restaurant;
    private RestaurantRequest request;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("Rua A", "1", null, "SP", "SP", "01310-000");
        owner = User.builder().id(1L).email("owner@test.com").role(UserRole.OWNER)
                .userType(new UserType(1L, "OWNER")).address(address).name("Owner").build();
        customer = User.builder().id(2L).email("customer@test.com").role(UserRole.CUSTOMER)
                .userType(new UserType(2L, "CUSTOMER")).address(address).name("Customer").build();
        restaurant = Restaurant.builder().id(1L).name("Cantina").address(address)
                .cuisineType("Italiana").openingTime(LocalTime.of(11, 0))
                .closingTime(LocalTime.of(23, 0)).owner(owner).build();
        request = new RestaurantRequest("Cantina", address, "Italiana",
                LocalTime.of(11, 0), LocalTime.of(23, 0));
    }

    @Test
    void create_success() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(restaurantRepository.save(any())).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        RestaurantResponse response = restaurantService.create("owner@test.com", request);

        assertThat(response.name()).isEqualTo("Cantina");
        assertThat(response.ownerName()).isEqualTo("Owner");
    }

    @Test
    void create_userNotFound_throws() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.create("x@x.com", request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void create_notOwnerRole_throws() {
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> restaurantService.create("customer@test.com", request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findById_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        RestaurantResponse response = restaurantService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void findById_notFound_throws() {
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.findById(99L))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void findAll_returnsPaginatedResult() {
        when(restaurantRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(restaurant)));

        Page<RestaurantResponse> result = restaurantService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void update_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RestaurantResponse response = restaurantService.update("owner@test.com", 1L,
                new RestaurantRequest("Cantina Nova", address, "Francesa",
                        LocalTime.of(12, 0), LocalTime.of(22, 0)));

        assertThat(response.name()).isEqualTo("Cantina Nova");
        assertThat(response.cuisineType()).isEqualTo("Francesa");
    }

    @Test
    void update_notOwner_throws() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.update("outro@test.com", 1L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_success() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        restaurantService.delete("owner@test.com", 1L);

        verify(restaurantRepository).delete(restaurant);
    }

    @Test
    void delete_notOwner_throws() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.delete("outro@test.com", 1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(restaurantRepository, never()).delete(any());
    }
}
