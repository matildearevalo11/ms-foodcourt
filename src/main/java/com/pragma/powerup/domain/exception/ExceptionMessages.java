package com.pragma.powerup.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessages {
    RESTAURANT_NAME_NUMERIC("Restaurant name cannot contain only numbers"),
    INVALID_NIT("NIT must contain only numbers"),
    INVALID_PHONE("Restaurant phone must contain up to 13 characters and only an optional leading + followed by numbers"),
    OWNER_ROLE_REQUIRED("The supplied user does not exist or does not have the OWNER role"),
    NIT_ALREADY_EXISTS("A restaurant with this NIT already exists"),
    USERS_SERVICE_UNAVAILABLE("User service is unavailable"),
    RESTAURANT_NOT_FOUND("Restaurant does not exist"),
    CATEGORY_NOT_FOUND("Category does not exist"),
    INVALID_DISH_PRICE("Dish price must be a positive integer greater than zero"),
    DISH_NOT_FOUND("Dish does not exist"),
    RESTAURANT_OWNER_REQUIRED("Only the restaurant owner can manage its dishes"),
    ACTIVE_ORDER_EXISTS("Customer already has an order in process"),
    ORDER_ITEMS_REQUIRED("Order must contain at least one dish"),
    DUPLICATED_ORDER_DISH("A dish cannot be repeated in the same order"),
    INVALID_ORDER_DISH("All dishes must be active and belong to the selected restaurant"),
    INVALID_ORDER_QUANTITY("Dish quantity must be greater than zero"),
    TRACEABILITY_SERVICE_UNAVAILABLE("Traceability service is unavailable"),
    AUTHENTICATED_USER_NOT_FOUND("Authenticated user not found"),
    INVALID_AUTHENTICATED_USER_ID("Invalid authenticated user identifier"),
    EMPLOYEE_RESTAURANT_NOT_ASSIGNED("Authenticated employee has no restaurant assigned"),
    ORDER_NOT_AVAILABLE_FOR_ASSIGNMENT("Order is not pending, is already assigned, or belongs to another restaurant"),
    ORDER_NOT_AVAILABLE_TO_MARK_READY("Order is not in preparation, is not assigned to the employee, or belongs to another restaurant"),
    CUSTOMER_CONTACT_UNAVAILABLE("Customer contact information is unavailable"),
    MESSAGING_SERVICE_UNAVAILABLE("Messaging service is unavailable"),
    ORDER_NOT_AVAILABLE_FOR_DELIVERY("Order is not ready, the security PIN is invalid, or the order is not assigned to the employee"),

    ;

    private final String message;
}
