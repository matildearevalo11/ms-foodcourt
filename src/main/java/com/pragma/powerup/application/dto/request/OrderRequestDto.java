package com.pragma.powerup.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record OrderRequestDto(
        @NotNull(message = "Restaurant id is required")
        @Positive(message = "Restaurant id must be positive")
        Long restaurantId,

        @NotEmpty(message = "Order must contain at least one dish")
        List<@Valid OrderItemRequestDto> items
) { }
