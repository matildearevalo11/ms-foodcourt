package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import java.util.Set;

public interface IOrderPersistencePort {

    boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses);
    Order saveOrder(Order order);
    PageResult<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, int page, int size);
}
