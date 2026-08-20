package com.restaurant.auth;

import com.restaurant.auth.dto.AuthResponse;
import com.restaurant.auth.dto.LoginRequest;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.security.JwtService;
import com.restaurant.security.UserPrincipal;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .login(request.login())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .address(request.address())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole());
    }
}
