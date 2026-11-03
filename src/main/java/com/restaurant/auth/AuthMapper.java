package com.restaurant.auth;

import com.restaurant.auth.dto.AuthResponse;
import com.restaurant.auth.dto.RegisterRequest;
import com.restaurant.security.UserPrincipal;
import com.restaurant.user.User;
import com.restaurant.user.UserRole;
import com.restaurant.usertype.InvalidUserTypeException;
import com.restaurant.usertype.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Responsável exclusivamente por conversões e montagem de objetos relacionados à autenticação.
 * Separa a lógica de mapeamento do fluxo de negócio do AuthService (SRP).
 */
@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final PasswordEncoder passwordEncoder;

    /** Constrói a entidade User a partir do request e dados já validados. */
    public User toUser(RegisterRequest request, UserType userType, UserRole role) {
        return User.builder()
                .name(request.name())
                .login(request.login())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .userType(userType)
                .address(request.address())
                .build();
    }

    /** Monta a resposta de autenticação a partir do usuário salvo e do token gerado. */
    public AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole(), user.getUserType());
    }

    /**
     * Mapeia o nome do UserType para o enum UserRole.
     * Lança InvalidUserTypeException se o nome não corresponder a nenhum role válido.
     */
    public UserRole resolveRole(String typeName) {
        try {
            return UserRole.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserTypeException(typeName);
        }
    }
}
