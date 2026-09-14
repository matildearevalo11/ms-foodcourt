package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Order;

public interface ITraceabilityPort {
    void registerPendingOrder(Order order);
}
