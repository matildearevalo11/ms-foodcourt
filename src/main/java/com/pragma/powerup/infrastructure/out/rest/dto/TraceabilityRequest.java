package com.pragma.powerup.infrastructure.out.rest.dto;

import com.pragma.powerup.domain.enums.OrderStatus;

public record TraceabilityRequest(Long orderId, Long customerId, Long restaurantId, Employee employee,
                                  OrderStatus previousStatus, OrderStatus newStatus) {
    public record Employee(Long id, String fullName) {
    }
}
