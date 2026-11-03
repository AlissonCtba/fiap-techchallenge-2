package com.restaurant.restaurant;

import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.UserType;
import com.restaurant.util.IntegrationTestBase;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;

class RestaurantControllerIT extends IntegrationTestBase {

    private String ownerToken;
    private String customerToken;

    private static final String RESTAURANT_BODY = """
            {
                "name": "Cantina",
                "address": {
                    "street": "Rua A", "number": "1",
                    "city": "SP", "state": "SP", "zipCode": "01310-000"
                },
                "cuisineType": "Italiana",
                "openingTime": "11:00:00",
                "closingTime": "23:00:00"
            }
            """;

    @BeforeEach
    void setUp() {
        UserType ownerType = createUserType("OWNER");
        UserType customerType = createUserType("CUSTOMER");
        User owner = createUser("owner@test.com", "owner", UserRole.OWNER, ownerType);
        User customer = createUser("customer@test.com", "customer", UserRole.CUSTOMER, customerType);
        ownerToken = tokenFor(owner);
        customerToken = tokenFor(customer);
    }

    @Test
    void create_asOwner_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .statusCode(201)
                .body("name", equalTo("Cantina"))
                .body("id", notNullValue());
    }

    @Test
    void create_asCustomer_forbidden() {
        RestAssuredMockMvc.given()
                .header("Authorization", customerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .statusCode(403);
    }

    @Test
    void create_noToken_unauthorized() {
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .statusCode(403);
    }

    @Test
    void findAll_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    void findById_success() {
        int id = RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .statusCode(201)
                .extract().path("id");

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id));
    }

    @Test
    void findById_notFound() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/999")
                .then()
                .statusCode(404);
    }

    @Test
    void update_success() {
        int id = RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .extract().path("id");

        String updateBody = """
                {
                    "name": "Cantina Nova",
                    "address": {
                        "street": "Rua B", "number": "2",
                        "city": "RJ", "state": "RJ", "zipCode": "20000-000"
                    },
                    "cuisineType": "Francesa",
                    "openingTime": "12:00:00",
                    "closingTime": "22:00:00"
                }
                """;

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updateBody)
                .when()
                .put("/api/v1/restaurants/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Cantina Nova"));
    }

    @Test
    void delete_success() {
        int id = RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .extract().path("id");

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .delete("/api/v1/restaurants/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    void searchByName_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .statusCode(201);

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/search/name?name=Cantina")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void searchByCuisine_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(RESTAURANT_BODY)
                .when()
                .post("/api/v1/restaurants")
                .then()
                .statusCode(201);

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/restaurants/search/cuisine?cuisineType=Italiana")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(1)));
    }
}
