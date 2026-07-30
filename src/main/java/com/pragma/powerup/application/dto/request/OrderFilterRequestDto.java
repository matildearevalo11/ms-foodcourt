package com.pragma.powerup.application.dto.request;

import com.pragma.powerup.domain.enums.OrderStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderFilterRequestDto {
    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @Min(value = 0, message = "Page must be zero or greater")
    private int page = 0;

    @Min(value = 1, message = "Size must be greater than zero")
    @Max(value = 100, message = "Size must be at most 100")
    private int size = 10;
}
