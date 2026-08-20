package com.restaurant.user;

import com.restaurant.auth.EmailAlreadyExistsException;
import com.restaurant.exception.LoginAlreadyExistsException;
import com.restaurant.exception.UserNotFoundException;
import com.restaurant.user.dto.ChangePasswordRequest;
import com.restaurant.user.dto.UpdateUserRequest;
import com.restaurant.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUser(String email, UpdateUserRequest request) {
        User user = findByEmail(email);

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new EmailAlreadyExistsException("E-mail já está em uso: " + request.email());
        }

        if (!user.getLogin().equals(request.login())
                && userRepository.existsByLoginAndIdNot(request.login(), user.getId())) {
            throw new LoginAlreadyExistsException("Login já está em uso: " + request.login());
        }

        user.setName(request.name());
        user.setLogin(request.login());
        user.setEmail(request.email());
        user.setAddress(request.address());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deleteByEmail(String email) {
        User user = findByEmail(email);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findByName(String name, Pageable pageable) {
        return userRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        return UserResponse.from(findByEmail(email));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + email));
    }
}
