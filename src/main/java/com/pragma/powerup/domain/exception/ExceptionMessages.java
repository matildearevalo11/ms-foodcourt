package com.pragma.powerup.domain.exception;

public enum ExceptionMessages {
    RESTAURANT_NAME_NUMERIC("Restaurant name cannot contain only numbers"),
    INVALID_NIT("NIT must contain only numbers"),
    INVALID_PHONE("Restaurant phone must contain up to 13 characters and only an optional leading + followed by numbers"),
    OWNER_ROLE_REQUIRED("The supplied user does not exist or does not have the OWNER role"),
    NIT_ALREADY_EXISTS("A restaurant with this NIT already exists"),
    USERS_SERVICE_UNAVAILABLE("User service is unavailable"),
    RESTAURANT_NOT_FOUND("Restaurant does not exist"),
    CATEGORY_NOT_FOUND("Category does not exist"),
    INVALID_DISH_PRICE("Dish price must be a positive integer greater than zero");

    private final String message;
    ExceptionMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
