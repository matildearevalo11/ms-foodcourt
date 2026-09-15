package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.PageResult;

public interface IOrderServicePort {
    Order createOrder(Order order);

    PageResult<Order> getOrdersByStatus(OrderStatus status, int page, int size);

    Order assignOrder(Long orderId);

    Order markOrderReady(Long orderId);

    Order deliverOrder(Long orderId, String securityPin);
}
