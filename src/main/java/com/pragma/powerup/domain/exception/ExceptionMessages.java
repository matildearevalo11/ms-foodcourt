package com.pragma.powerup.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessages {
    OWNER_ROLE_REQUIRED("The supplied user does not exist or does not have the OWNER role"),
    NIT_ALREADY_EXISTS("A restaurant with this NIT already exists"),
    USERS_SERVICE_UNAVAILABLE("User service is unavailable"),
    RESTAURANT_NOT_FOUND("The requested restaurant does not exist"),
    CATEGORY_NOT_FOUND("The requested category does not exist"),
    DISH_NOT_FOUND("The requested dish does not exist"),
    RESTAURANT_OWNER_REQUIRED("Only the restaurant owner can manage its dishes"),
    AUTHENTICATED_USER_NOT_FOUND("Authenticated user not found"),
    INVALID_AUTHENTICATED_USER_ID("Invalid authenticated user identifier"),
    ACCESS_DENIED("The authenticated user does not have permission to perform this action");

    private final String message;
}
