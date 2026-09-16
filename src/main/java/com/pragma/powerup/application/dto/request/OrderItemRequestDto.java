package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequestDto(
        @NotNull(message = "Dish id is required")
        @Positive(message = "Dish id must be positive")
        Long dishId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity
) { }
