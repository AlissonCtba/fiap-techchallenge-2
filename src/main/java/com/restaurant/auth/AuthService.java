package com.restaurant.auth;

import com.restaurant.auth.dto.AuthResponse;
import com.restaurant.auth.dto.LoginRequest;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.security.JwtService;
import com.restaurant.security.UserPrincipal;
import com.restaurant.user.User;
import com.restaurant.user.UserRepository;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.InvalidUserTypeException;
import com.restaurant.usertype.UserType;
import com.restaurant.usertype.UserTypeNotFoundException;
import com.restaurant.usertype.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado: " + request.email());
        }

        UserType userType = userTypeRepository.findById(request.userTypeId())
                .orElseThrow(() -> new UserTypeNotFoundException(
                        "Tipo de usuário não encontrado: " + request.userTypeId()));

        // Infere o enum de role pelo nome do tipo para manter a lógica de segurança
        UserRole role = resolveRole(userType.getName());

        User user = User.builder()
                .name(request.name())
                .login(request.login())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .userType(userType)
                .address(request.address())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole(), userType);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole(), user.getUserType());
    }

    /**
     * Mapeia o nome do UserType para o enum UserRole.
     * Tipos com nomes fora do enum existem no cadastro mas não podem ser associados a usuários.
     */
    private UserRole resolveRole(String typeName) {
        try {
            return UserRole.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserTypeException(typeName);
        }
    }
}
