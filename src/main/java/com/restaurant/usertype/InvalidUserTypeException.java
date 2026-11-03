package com.restaurant.usertype;

import com.restaurant.user.UserRole;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InvalidUserTypeException extends RuntimeException {
    public InvalidUserTypeException(String typeName) {
        super("O tipo de usuário '" + typeName + "' não pode ser associado a usuários. "
                + "Tipos válidos: " + validNames());
    }

    private static String validNames() {
        return Arrays.stream(UserRole.values())
                .map(UserRole::name)
                .collect(Collectors.joining(", "));
    }
}
