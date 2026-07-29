package com.pragma.powerup.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderRequestDto {

    @NotNull(message = "Restaurant id is required")
    @Positive(message = "Restaurant id must be greater than zero")
    private Long restaurantId;

    @NotEmpty(message = "Order must contain at least one dish")
    private List<@Valid OrderItemRequestDto> items;
}
