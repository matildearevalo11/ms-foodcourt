package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import java.util.Set;
import java.util.Optional;
public interface IOrderPersistencePort {

    boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses);
    Order saveOrder(Order order);
    PageResult<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, int page, int size);
    Optional<Order> assignPendingOrder(Long orderId, Long restaurantId, Long employeeId);
    Optional<Order> markOrderReady(Long orderId, Long restaurantId, Long employeeId, String securityPin);
    Optional<Order> deliverReadyOrder(Long orderId, Long restaurantId, Long employeeId, String securityPin);
}
