package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.enums.OrderStatus;

public interface ITraceabilityPort {
    void registerStatusChange(Order order, OrderStatus previousStatus, Long employeeId);
}
