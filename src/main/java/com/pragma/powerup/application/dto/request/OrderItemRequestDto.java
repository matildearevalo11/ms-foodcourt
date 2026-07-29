package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDto {
    @NotNull(message = "Dish id is required")
    @Positive(message = "Dish id must be greater than zero")
    private Long dishId;

    @NotNull(message = "Dish quantity is required")
    @Positive(message = "Dish quantity must be greater than zero")
    private Integer quantity;
}
