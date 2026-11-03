package com.restaurant.auth;

import com.restaurant.auth.dto.AuthResponse;
import com.restaurant.auth.dto.LoginRequest;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.security.JwtService;
import com.restaurant.security.UserPrincipal;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.UserType;
import com.restaurant.usertype.UserTypeNotFoundException;
import com.restaurant.usertype.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Orquestra os fluxos de negócio de registro e login.
 * Conversões e montagem de objetos são delegadas ao AuthMapper (SRP).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + request.email());
        }

        UserType userType = userTypeRepository.findById(request.userTypeId())
                .orElseThrow(() -> new UserTypeNotFoundException(
                        "Tipo de usuário não encontrado: " + request.userTypeId()));

        UserRole role = authMapper.resolveRole(userType.getName());
        User user = userRepository.save(authMapper.toUser(request, userType, role));
        String token = jwtService.generateToken(new UserPrincipal(user));

        return authMapper.toAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(new UserPrincipal(user));

        return authMapper.toAuthResponse(user, token);
    }
}
