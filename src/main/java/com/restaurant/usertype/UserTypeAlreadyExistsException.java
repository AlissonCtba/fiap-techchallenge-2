package com.restaurant.usertype;

public class UserTypeAlreadyExistsException extends RuntimeException {
    public UserTypeAlreadyExistsException(String message) {
        super(message);
    }
}
