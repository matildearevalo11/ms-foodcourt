package com.pragma.powerup.application.dto.response;

import com.pragma.powerup.domain.enums.OrderStatus;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(Long id, Long customerId, Long restaurantId, OrderStatus status,
        Instant createdAt, List<OrderItemResponseDto> items) { }
