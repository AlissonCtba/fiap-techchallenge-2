package com.restaurant.auth;

import com.restaurant.auth.dto.AuthResponse;
import com.restaurant.auth.dto.LoginRequest;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.security.JwtService;
import com.restaurant.security.UserPrincipal;
import com.restaurant.user.Address;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.InvalidUserTypeException;
import com.restaurant.usertype.UserType;
import com.restaurant.usertype.UserTypeNotFoundException;
import com.restaurant.usertype.UserTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserTypeRepository userTypeRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @InjectMocks AuthService authService;

    private Address address;
    private UserType ownerType;

    @BeforeEach
    void setUp() {
        address = new Address("Rua A", "1", null, "SP", "SP", "01310-000");
        ownerType = new UserType(1L, "OWNER");
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(false);
        when(userTypeRepository.findById(1L)).thenReturn(Optional.of(ownerType));
        when(passwordEncoder.encode("senha123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        RegisterRequest req = new RegisterRequest("João", "joao", "joao@test.com", "senha123", 1L, address);
        AuthResponse response = authService.register(req);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(UserRole.OWNER);
    }

    @Test
    void register_emailAlreadyExists_throws() {
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest("João", "joao", "joao@test.com", "senha123", 1L, address);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_userTypeNotFound_throws() {
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(false);
        when(userTypeRepository.findById(99L)).thenReturn(Optional.empty());

        RegisterRequest req = new RegisterRequest("João", "joao", "joao@test.com", "senha123", 99L, address);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserTypeNotFoundException.class);
    }

    @Test
    void register_invalidUserType_throws() {
        UserType invalidType = new UserType(3L, "GERENTE");
        when(userRepository.existsByEmail("joao@test.com")).thenReturn(false);
        when(userTypeRepository.findById(3L)).thenReturn(Optional.of(invalidType));

        RegisterRequest req = new RegisterRequest("João", "joao", "joao@test.com", "senha123", 3L, address);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(InvalidUserTypeException.class);
    }

    @Test
    void login_success() {
        User user = User.builder()
                .email("joao@test.com").name("João").password("encoded")
                .role(UserRole.OWNER).userType(ownerType).address(address).build();

        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("joao@test.com", "senha123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_wrongPassword_throws() {
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(new LoginRequest("joao@test.com", "errada")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
