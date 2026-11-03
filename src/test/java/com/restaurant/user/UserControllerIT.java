package com.restaurant.user;

import com.restaurant.util.IntegrationTestBase;
import com.restaurant.usertype.UserType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;

class UserControllerIT extends IntegrationTestBase {

    private String ownerToken;

    @BeforeEach
    void setUp() {
        UserType ownerType = createUserType("OWNER");
        User owner = createUser("owner@test.com", "owner", UserRole.OWNER, ownerType);
        ownerToken = tokenFor(owner);
    }

    @Test
    void getProfile_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(200)
                .body("email", equalTo("owner@test.com"));
    }

    @Test
    void updateUser_success() {
        String body = """
            {
                "name": "Owner Updated",
                "login": "owner",
                "email": "owner@test.com",
                "address": {
                    "street": "Rua B", "number": "2",
                    "city": "RJ", "state": "RJ", "zipCode": "20000-000"
                }
            }
            """;

        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when()
                .patch("/api/v1/users/me")
                .then()
                .statusCode(200)
                .body("name", equalTo("Owner Updated"));
    }

    @Test
    void changePassword_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"oldPassword\": \"senha123\", \"newPassword\": \"nova456\"}")
                .when()
                .patch("/api/v1/users/me/password")
                .then()
                .statusCode(200)
                .body("message", notNullValue());
    }

    @Test
    void deleteAccount_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .delete("/api/v1/users/me")
                .then()
                .statusCode(200)
                .body("message", notNullValue());
    }

    @Test
    void searchByName_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", ownerToken)
                .when()
                .get("/api/v1/users/search?name=Test")
                .then()
                .statusCode(200)
                .body("content", hasSize(greaterThanOrEqualTo(1)));
    }
}
