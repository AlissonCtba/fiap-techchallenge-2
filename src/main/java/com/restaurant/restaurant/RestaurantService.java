package com.restaurant.restaurant;

import com.restaurant.exception.UserNotFoundException;
import com.restaurant.restaurant.dto.RestaurantRequest;
import com.restaurant.restaurant.dto.RestaurantResponse;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
import com.restaurant.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public RestaurantResponse create(String ownerEmail, RestaurantRequest request) {
        User owner = findOwner(ownerEmail);

        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .address(request.address())
                .cuisineType(request.cuisineType())
                .openingTime(request.openingTime())
                .closingTime(request.closingTime())
                .owner(owner)
                .build();

        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse findById(Long id) {
        return RestaurantResponse.from(getById(id));
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> findAll(Pageable pageable) {
        return restaurantRepository.findAll(pageable).map(RestaurantResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> searchByName(String name, Pageable pageable) {
        return restaurantRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(RestaurantResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> searchByCuisine(String cuisineType, Pageable pageable) {
        return restaurantRepository.findByCuisineTypeContainingIgnoreCase(cuisineType, pageable)
                .map(RestaurantResponse::from);
    }

    @Transactional
    public RestaurantResponse update(String ownerEmail, Long id, RestaurantRequest request) {
        Restaurant restaurant = getById(id);
        assertOwner(restaurant, ownerEmail);

        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        restaurant.setCuisineType(request.cuisineType());
        restaurant.setOpeningTime(request.openingTime());
        restaurant.setClosingTime(request.closingTime());

        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void delete(String ownerEmail, Long id) {
        Restaurant restaurant = getById(id);
        assertOwner(restaurant, ownerEmail);
        restaurantRepository.delete(restaurant);
    }

    private Restaurant getById(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurante não encontrado: " + id));
    }

    private User findOwner(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + email));
        if (user.getRole() != UserRole.OWNER) {
            throw new AccessDeniedException("Apenas donos de restaurante podem gerenciar restaurantes");
        }
        return user;
    }

    private void assertOwner(Restaurant restaurant, String email) {
        if (!restaurant.getOwner().getEmail().equals(email)) {
            throw new AccessDeniedException("Você não é o dono deste restaurante");
        }
    }
}
