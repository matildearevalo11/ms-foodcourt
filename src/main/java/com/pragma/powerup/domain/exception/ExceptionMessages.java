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
    DISH_NOT_FOUND("The requested dish does not exist");

    private final String message;
}
