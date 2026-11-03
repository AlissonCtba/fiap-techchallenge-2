package com.restaurant.security;

import com.restaurant.util.IntegrationTestBase;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.Test;

class JwtAuthFilterIT extends IntegrationTestBase {

    @Test
    void invalidToken_proceedsWithoutAuth() {
        // JWT estruturalmente válido mas assinado com chave errada
        String wrongKeyToken = "eyJhbGciOiJIUzM4NCJ9" +
                ".eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDF9" +
                ".assinatura_invalida_aqui_que_vai_falhar";

        RestAssuredMockMvc.given()
                .header("Authorization", "Bearer " + wrongKeyToken)
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(403);
    }

    @Test
    void noAuthHeader_proceedsWithoutAuth() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(403);
    }

    @Test
    void authHeaderWithoutBearer_proceedsWithoutAuth() {
        RestAssuredMockMvc.given()
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .when()
                .get("/api/v1/users/me")
                .then()
                .statusCode(403);
    }
}
