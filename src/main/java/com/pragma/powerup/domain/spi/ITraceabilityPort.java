package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;

public interface ITraceabilityPort {
    void registerStatusChange(Order order, OrderStatus previousStatus);
}
