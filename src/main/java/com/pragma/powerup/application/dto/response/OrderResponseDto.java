package com.pragma.powerup.application.dto.response;

import com.pragma.powerup.domain.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDto {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> items;
}
