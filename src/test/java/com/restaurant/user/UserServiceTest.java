package com.restaurant.user;

import com.restaurant.auth.EmailAlreadyExistsException;
import com.restaurant.exception.LoginAlreadyExistsException;
import com.restaurant.exception.UserNotFoundException;
import com.restaurant.user.dto.ChangePasswordRequest;
import com.restaurant.user.dto.UpdateUserRequest;
import com.restaurant.user.dto.UserResponse;
import com.restaurant.usertype.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address("Rua A", "1", null, "SP", "SP", "01310-000");
        user = User.builder()
                .id(1L).name("João").login("joao").email("joao@test.com")
                .password("encoded").role(UserRole.OWNER)
                .userType(new UserType(1L, "OWNER")).address(address)
                .build();
    }

    @Test
    void getProfile_success() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile("joao@test.com");

        assertThat(response.email()).isEqualTo("joao@test.com");
        assertThat(response.name()).isEqualTo("João");
    }

    @Test
    void getProfile_notFound_throws() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("x@x.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_success() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // mesmo email e login — não dispara checagem de duplicata
        UpdateUserRequest req = new UpdateUserRequest("João Novo", "joao", "joao@test.com", address);
        UserResponse response = userService.updateUser("joao@test.com", req);

        assertThat(response.name()).isEqualTo("João Novo");
    }

    @Test
    void updateUser_emailInUse_throws() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("outro@test.com", 1L)).thenReturn(true);

        UpdateUserRequest req = new UpdateUserRequest("João", "joao", "outro@test.com", address);

        assertThatThrownBy(() -> userService.updateUser("joao@test.com", req))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void updateUser_loginInUse_throws() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));
        // email não muda, então só a checagem de login é executada
        when(userRepository.existsByLoginAndIdNot("outro", 1L)).thenReturn(true);

        UpdateUserRequest req = new UpdateUserRequest("João", "outro", "joao@test.com", address);

        assertThatThrownBy(() -> userService.updateUser("joao@test.com", req))
                .isInstanceOf(LoginAlreadyExistsException.class);
    }

    @Test
    void changePassword_success() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("nova123")).thenReturn("nova-encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword("joao@test.com", new ChangePasswordRequest("senha123", "nova123"));

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("nova-encoded");
    }

    @Test
    void changePassword_wrongOldPassword_throws() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("joao@test.com",
                new ChangePasswordRequest("errada", "nova123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deleteByEmail_success() {
        when(userRepository.findByEmail("joao@test.com")).thenReturn(Optional.of(user));

        userService.deleteByEmail("joao@test.com");

        verify(userRepository).delete(user);
    }

    @Test
    void findByName_returnsPaginatedResult() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByNameContainingIgnoreCase(eq("João"), any()))
                .thenReturn(page);

        Page<UserResponse> result = userService.findByName("João", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("João");
    }
}
