package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record DishStatusRequestDto(
        @NotNull(message = "Dish status is required")
        Boolean active
) {
}
