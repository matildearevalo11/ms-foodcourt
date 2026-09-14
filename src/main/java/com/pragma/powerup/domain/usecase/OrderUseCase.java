package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderUseCase implements IOrderServicePort {
    private final IOrderPersistencePort orderPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ILoggedUserPort loggedUserPort;
    private final ITraceabilityPort traceabilityPort;

    @Override
    public Order createOrder(Order order) {
        Long customerId = loggedUserPort.getUserId();
        validateActiveOrder(customerId);
        validateRestaurant(order.getRestaurantId());
        validateItems(order.getRestaurantId(), order.getItems());
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());
        Order savedOrder = orderPersistencePort.save(order);
        traceabilityPort.registerPendingOrder(savedOrder);
        return savedOrder;
    }

    @Override
    public PageResult<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        return orderPersistencePort.findByRestaurantIdAndStatus(loggedUserPort.getRestaurantId(), status, page, size);
    }

    private void validateActiveOrder(Long customerId) {
        if (orderPersistencePort.existsByCustomerIdAndStatusIn(customerId, OrderStatus.activeStatuses())) {
            throw new ValidationException(ExceptionMessages.ACTIVE_ORDER_EXISTS.getMessage());
        }
    }

    private void validateRestaurant(Long restaurantId) {
        if (restaurantPersistencePort.findById(restaurantId).isEmpty()) {
            throw new NotFoundException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage());
        }
    }

    private void validateItems(Long restaurantId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException(ExceptionMessages.ORDER_ITEMS_REQUIRED.getMessage());
        }
        if (items.stream().anyMatch(item -> item.getQuantity() == null || item.getQuantity() <= 0)) {
            throw new ValidationException(ExceptionMessages.INVALID_ORDER_QUANTITY.getMessage());
        }

        List<Long> selectedIds = items.stream().map(OrderItem::getDishId).toList();
        if (selectedIds.stream().anyMatch(Objects::isNull)) {
            throw new ValidationException(ExceptionMessages.INVALID_ORDER_DISH.getMessage());
        }

        Set<Long> dishIds = new HashSet<>(selectedIds);
        if (dishIds.size() != selectedIds.size()) {
            throw new ValidationException(ExceptionMessages.DUPLICATED_ORDER_DISH.getMessage());
        }

        List<Dish> dishes = dishPersistencePort.findAllById(dishIds);
        boolean validDishes = dishes.size() == dishIds.size()
                && dishes.stream().allMatch(dish -> dish.isActive()
                && restaurantId.equals(dish.getRestaurantId()));
        if (!validDishes) {
            throw new ValidationException(ExceptionMessages.INVALID_ORDER_DISH.getMessage());
        }
    }
}
