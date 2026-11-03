package com.restaurant.util;

import com.restaurant.security.JwtService;
import com.restaurant.security.UserPrincipal;
import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.UserType;
import com.restaurant.usertype.UserTypeRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class IntegrationTestBase {

    @Autowired protected WebApplicationContext webApplicationContext;
    @Autowired protected JwtService jwtService;
    @Autowired protected UserRepository userRepository;
    @Autowired protected UserTypeRepository userTypeRepository;
    @Autowired protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpRestAssured() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    protected Address defaultAddress() {
        return new Address("Rua A", "1", null, "SP", "SP", "01310-000");
    }

    protected UserType createUserType(String name) {
        return userTypeRepository.save(UserType.builder().name(name).build());
    }

    protected User createUser(String email, String login, UserRole role, UserType userType) {
        return userRepository.save(User.builder()
                .name("Test User").login(login).email(email)
                .password(passwordEncoder.encode("senha123"))
                .role(role).userType(userType).address(defaultAddress())
                .build());
    }

    protected String tokenFor(User user) {
        return "Bearer " + jwtService.generateToken(new UserPrincipal(user));
    }
}
