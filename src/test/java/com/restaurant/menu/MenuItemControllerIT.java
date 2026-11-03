package com.restaurant.menu;

import com.restaurant.restaurant.Restaurant;
import com.restaurant.restaurant.RestaurantRepository;
import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.UserType;
import com.restaurant.util.IntegrationTestBase;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalTime;

import static org.hamcrest.Matchers.*;

class MenuItemControllerIT extends IntegrationTestBase {

    @Autowired RestaurantRepository restaurantRepository;

    private String ownerToken;
    private Long restaurantId;

    private static final String ITEM_BODY = """
            {
                "name": "Lasanha",
                "description": "Deliciosa",
                "price": 45.90,
                "dineInOnly": false,
                "photoPath": "/img/lasanha.jpg"
            }
            """;

    @BeforeEach
    void setUp() {
        UserType ownerType = createUserType("OWNER");
        User owner = createUser("owner@test.com", "owner", UserRole.OWNER, ownerType);
        ownerToken = tokenFor(owner);

        Address address = defaultAddress();
        Restaurant restaurant = restaurantRepository.save(Restaurant.builder()
                .name("Cantina").address(address).cuisineType("Italiana")
                .openingTime(LocalTime.of(11, 0)).closingTime(LocalTime.of(23, 0))
                .owner(owner).build());
        restaurantId = restaurant.getId();
    }

    @Test
    void create_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(ITEM_BODY)
                .when()
                .post("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .statusCode(201)
                .body("name", equalTo("Lasanha"))
                .body("id", notNullValue());
    }

    @Test
    void create_invalidBody_badRequest() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\": \"\"}")
                .when()
                .post("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .statusCode(400);
    }

    @Test
    void findAll_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void findById_success() {
        int itemId = RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(ITEM_BODY)
                .when()
                .post("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .extract().path("id");

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/" + restaurantId + "/menu/" + itemId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Lasanha"));
    }

    @Test
    void findById_notFound() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/" + restaurantId + "/menu/999")
                .then()
                .statusCode(404);
    }

    @Test
    void update_success() {
        int itemId = RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(ITEM_BODY)
                .when()
                .post("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .extract().path("id");

        String updateBody = """
                {
                    "name": "Lasanha Premium",
                    "description": "Melhor",
                    "price": 59.90,
                    "dineInOnly": true,
                    "photoPath": "/img/premium.jpg"
                }
                """;

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updateBody)
                .when()
                .put("/api/v1/restaurants/" + restaurantId + "/menu/" + itemId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Lasanha Premium"))
                .body("price", equalTo(59.90f));
    }

    @Test
    void delete_success() {
        int itemId = RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(ITEM_BODY)
                .when()
                .post("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .extract().path("id");

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .delete("/api/v1/restaurants/" + restaurantId + "/menu/" + itemId)
                .then()
                .statusCode(204);
    }

    @Test
    void searchByName_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(ITEM_BODY)
                .when()
                .post("/api/v1/restaurants/" + restaurantId + "/menu")
                .then()
                .statusCode(201);

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/" + restaurantId + "/menu/search?name=Lasanha")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(1)));
    }
}
