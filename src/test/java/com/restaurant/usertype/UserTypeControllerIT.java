package com.restaurant.usertype;

import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.util.IntegrationTestBase;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;

class UserTypeControllerIT extends IntegrationTestBase {

    private String token;

    @BeforeEach
    void setUp() {
        UserType ownerType = createUserType("OWNER");
        User owner = createUser("owner@test.com", "owner", UserRole.OWNER, ownerType);
        token = tokenFor(owner);
    }

    @Test
    void create_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\": \"MANAGER\"}")
                .when()
                .post("/api/v1/user-types")
                .then()
                .statusCode(201)
                .body("name", equalTo("MANAGER"))
                .body("id", notNullValue());
    }

    @Test
    void create_duplicate_conflict() {
        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\": \"OWNER\"}")
                .when()
                .post("/api/v1/user-types")
                .then()
                .statusCode(409);
    }

    @Test
    void findAll_success() {
        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .when()
                .get("/api/v1/user-types")
                .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void findById_success() {
        UserType type = createUserType("CUSTOMER");

        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .when()
                .get("/api/v1/user-types/" + type.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("CUSTOMER"));
    }

    @Test
    void findById_notFound() {
        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .when()
                .get("/api/v1/user-types/999")
                .then()
                .statusCode(404);
    }

    @Test
    void update_success() {
        UserType type = createUserType("TEMP");

        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"name\": \"UPDATED\"}")
                .when()
                .put("/api/v1/user-types/" + type.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo("UPDATED"));
    }

    @Test
    void delete_success() {
        UserType type = createUserType("TO_DELETE");

        RestAssuredMockMvc.given()
                .header("Authorization", token)
                .when()
                .delete("/api/v1/user-types/" + type.getId())
                .then()
                .statusCode(204);
    }

    @Test
    void noToken_unauthorized() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/v1/user-types")
                .then()
                .statusCode(403);
    }
}
