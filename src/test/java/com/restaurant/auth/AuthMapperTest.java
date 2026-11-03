package com.restaurant.auth;

import com.restaurant.auth.dto.AuthResponse;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.InvalidUserTypeException;
import com.restaurant.usertype.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthMapperTest {

    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AuthMapper authMapper;

    private Address address;
    private UserType ownerType;

    @BeforeEach
    void setUp() {
        address = new Address("Rua A", "1", null, "SP", "SP", "01310-000");
        ownerType = new UserType(1L, "OWNER");
    }

    @Test
    void resolveRole_validName_returnsRole() {
        assertThat(authMapper.resolveRole("OWNER")).isEqualTo(UserRole.OWNER);
        assertThat(authMapper.resolveRole("owner")).isEqualTo(UserRole.OWNER);
        assertThat(authMapper.resolveRole("CUSTOMER")).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void resolveRole_invalidName_throws() {
        assertThatThrownBy(() -> authMapper.resolveRole("GERENTE"))
                .isInstanceOf(InvalidUserTypeException.class);
    }

    @Test
    void toUser_buildsEntityCorrectly() {
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");

        RegisterRequest req = new RegisterRequest("João", "joao", "joao@test.com", "senha123", 1L, address);
        User user = authMapper.toUser(req, ownerType, UserRole.OWNER);

        assertThat(user.getName()).isEqualTo("João");
        assertThat(user.getLogin()).isEqualTo("joao");
        assertThat(user.getEmail()).isEqualTo("joao@test.com");
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getRole()).isEqualTo(UserRole.OWNER);
        assertThat(user.getUserType()).isEqualTo(ownerType);
        assertThat(user.getAddress()).isEqualTo(address);
    }

    @Test
    void toAuthResponse_buildsResponseCorrectly() {
        User user = User.builder()
                .email("joao@test.com").name("João")
                .role(UserRole.OWNER).userType(ownerType).address(address).build();

        AuthResponse response = authMapper.toAuthResponse(user, "jwt-token");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("joao@test.com");
        assertThat(response.name()).isEqualTo("João");
        assertThat(response.role()).isEqualTo(UserRole.OWNER);
        assertThat(response.userType().name()).isEqualTo("OWNER");
    }
}
