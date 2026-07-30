package com.pragma.powerup.infrastructure.out.rest.dto;

import com.pragma.powerup.domain.enums.OrderStatus;

public record TraceabilityRequest(Long orderId, Long customerId, Long restaurantId,
                                  OrderStatus previousStatus, OrderStatus newStatus) {
}
