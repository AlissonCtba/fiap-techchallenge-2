package com.restaurant.auth;

import com.restaurant.util.IntegrationTestBase;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;

class AuthControllerIT extends IntegrationTestBase {

    private Long ownerTypeId;

    @BeforeEach
    void setUp() {
        ownerTypeId = createUserType("OWNER").getId();
        createUserType("CUSTOMER");
    }

    @Test
    void register_success() {
        String body = """
            {
                "name": "João",
                "login": "joao",
                "email": "joao@test.com",
                "password": "senha123",
                "userTypeId": %d,
                "address": {
                    "street": "Rua A", "number": "1",
                    "city": "SP", "state": "SP", "zipCode": "01310-000"
                }
            }
            """.formatted(ownerTypeId);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when()
                .post("/api/v1/auth/register")
                .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("role", equalTo("OWNER"));
    }

    @Test
    void register_duplicateEmail_conflict() {
        var type = userTypeRepository.findById(ownerTypeId).orElseThrow();
        createUser("joao@test.com", "joao", com.restaurant.user.UserRole.OWNER, type);

        String body = """
            {
                "name": "João2",
                "login": "joao2",
                "email": "joao@test.com",
                "password": "senha123",
                "userTypeId": %d,
                "address": {
                    "street": "Rua A", "number": "1",
                    "city": "SP", "state": "SP", "zipCode": "01310-000"
                }
            }
            """.formatted(ownerTypeId);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .when()
                .post("/api/v1/auth/register")
                .then()
                .statusCode(409);
    }

    @Test
    void register_missingFields_badRequest() {
        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"email\": \"x@x.com\"}")
                .when()
                .post("/api/v1/auth/register")
                .then()
                .statusCode(400)
                .body("fields", notNullValue());
    }

    @Test
    void login_success() {
        var type = userTypeRepository.findById(ownerTypeId).orElseThrow();
        createUser("joao@test.com", "joao", com.restaurant.user.UserRole.OWNER, type);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"email\": \"joao@test.com\", \"password\": \"senha123\"}")
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void login_wrongPassword_unauthorized() {
        var type = userTypeRepository.findById(ownerTypeId).orElseThrow();
        createUser("joao@test.com", "joao", com.restaurant.user.UserRole.OWNER, type);

        RestAssuredMockMvc.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{\"email\": \"joao@test.com\", \"password\": \"errada\"}")
                .when()
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401);
    }
}
