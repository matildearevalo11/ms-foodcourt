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
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.domain.spi.IPinGeneratorPort;
import com.pragma.powerup.domain.spi.IUserContactPort;
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
    private final IPinGeneratorPort pinGeneratorPort;
    private final IUserContactPort userContactPort;
    private final INotificationPort notificationPort;

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
        traceabilityPort.registerStatusChange(savedOrder, null, null);
        return savedOrder;
    }

    @Override
    public PageResult<Order> getOrdersByStatus(OrderStatus status, int page, int size) {
        return orderPersistencePort.findByRestaurantIdAndStatus(loggedUserPort.getRestaurantId(), status, page, size);
    }

    @Override
    public Order assignOrder(Long orderId) {
        Long employeeId = loggedUserPort.getUserId();
        Order order = orderPersistencePort.assignPendingOrder(orderId, loggedUserPort.getRestaurantId(), employeeId)
                .orElseThrow(() -> new ValidationException(ExceptionMessages.ORDER_NOT_AVAILABLE_FOR_ASSIGNMENT.getMessage()));
        traceabilityPort.registerStatusChange(order, OrderStatus.PENDING, employeeId);
        return order;
    }

    @Override
    public Order markOrderReady(Long orderId) {
        Long employeeId = loggedUserPort.getUserId();
        String securityPin = pinGeneratorPort.generate();
        Order order = orderPersistencePort.markOrderReady(orderId, loggedUserPort.getRestaurantId(), employeeId, securityPin)
                .orElseThrow(() -> new ValidationException(ExceptionMessages.ORDER_NOT_AVAILABLE_TO_MARK_READY.getMessage()));
        String cellphone = userContactPort.getCustomerCellphone(order.getCustomerId());
        notificationPort.notifyOrderReady(cellphone, securityPin);
        traceabilityPort.registerStatusChange(order, OrderStatus.IN_PREPARATION, employeeId);
        return order;
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
