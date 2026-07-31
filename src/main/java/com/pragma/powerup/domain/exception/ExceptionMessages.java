package com.pragma.powerup.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionMessages {
    RESTAURANT_NAME_NUMERIC("FC-001", "Restaurant name cannot contain only numbers"),
    INVALID_NIT("FC-002", "NIT must contain only numbers"),
    INVALID_PHONE("FC-003", "Restaurant phone must contain up to 13 characters and only an optional leading + followed by numbers"),
    OWNER_ROLE_REQUIRED("FC-004", "The supplied user does not exist or does not have the OWNER role"),
    NIT_ALREADY_EXISTS("FC-005", "A restaurant with this NIT already exists"),
    USERS_SERVICE_UNAVAILABLE("FC-006", "User service is unavailable"),
    RESTAURANT_NOT_FOUND("FC-007", "Restaurant does not exist"),
    CATEGORY_NOT_FOUND("FC-008", "Category does not exist"),
    INVALID_DISH_PRICE("FC-009", "Dish price must be a positive integer greater than zero"),
    DISH_NOT_FOUND("FC-010", "Dish does not exist"),
    RESTAURANT_OWNER_REQUIRED("FC-011", "Only the restaurant owner can manage its dishes"),
    ACTIVE_ORDER_EXISTS("FC-012", "Customer already has an order in process"),
    ORDER_ITEMS_REQUIRED("FC-013", "Order must contain at least one dish"),
    DUPLICATED_ORDER_DISH("FC-014", "A dish cannot be repeated in the same order"),
    INVALID_ORDER_DISH("FC-015", "All dishes must be active and belong to the selected restaurant"),
    INVALID_ORDER_QUANTITY("FC-016", "Dish quantity must be greater than zero"),
    TRACEABILITY_SERVICE_UNAVAILABLE("FC-017", "Traceability service is unavailable"),
    ORDER_NOT_AVAILABLE_FOR_ASSIGNMENT("FC-021", "Order is not pending, is already assigned, or belongs to another restaurant"),
    ORDER_NOT_AVAILABLE_TO_MARK_READY("FC-022", "Order is not in preparation, is not assigned to the employee, or belongs to another restaurant"),
    CUSTOMER_CONTACT_UNAVAILABLE("FC-023", "Customer contact information is unavailable"),
    MESSAGING_SERVICE_UNAVAILABLE("FC-024", "Messaging service is unavailable"),
    ORDER_NOT_AVAILABLE_FOR_DELIVERY("FC-025", "Order is not ready, the security PIN is invalid, or the order is not assigned to the employee"),
    ORDER_CANNOT_BE_CANCELED("FC-026", "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse"),
    AUTHENTICATION_REQUIRED("SEC-001", "Authentication required"),
    ACCESS_DENIED("SEC-002", "Access denied"),
    AUTHENTICATED_USER_NOT_FOUND("SEC-003", "Authenticated user not found"),
    INVALID_AUTHENTICATED_USER_ID("SEC-004", "Invalid authenticated user identifier"),
    EMPLOYEE_RESTAURANT_NOT_ASSIGNED("SEC-005", "Authenticated employee has no restaurant assigned"),
    AUTHENTICATED_EMPLOYEE_NAME_NOT_FOUND("SEC-006", "Authenticated employee name not found"),

    ;

    private final String code;
    private final String message;

    public static String codeFor(String message) {
        for (ExceptionMessages exceptionMessage : values()) {
            if (exceptionMessage.message.equals(message)) {
                return exceptionMessage.code;
            }
        }
        return "FC-000";
    }
}
