package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DishUpdateRequestDto(
        @NotNull(message = "Dish price is required")
        @Positive(message = "Dish price must be greater than zero")
        Long price,

        @NotBlank(message = "Description is required")
        String description
) {
}
