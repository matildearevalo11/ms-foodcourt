package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.enums.OrderStatus;

public interface IOrderServicePort {
    Order saveOrder(Order order);
    PageResult<Order> getOrders(OrderStatus status, int page, int size);
    Order assignOrder(Long orderId);
    Order markOrderReady(Long orderId);
    Order deliverOrder(Long orderId, String securityPin);
    Order cancelOrder(Long orderId);
}
