package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class OrderJpaAdapter implements IOrderPersistencePort {
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository itemRepository;
    private final IOrderEntityMapper orderMapper;
    private final IOrderItemEntityMapper itemMapper;

    @Override
    public boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses) {
        return orderRepository.existsByCustomerIdAndStatusIn(customerId, statuses);
    }

    @Override
    public Order save(Order order) {
        try {
            OrderEntity entity = orderMapper.toEntity(order);
            entity.setActiveOrder(order.getStatus().isActive() ? Boolean.TRUE : null);
            OrderEntity savedOrder = orderRepository.saveAndFlush(entity);

            List<OrderItemEntity> items = itemMapper.toEntityList(order.getItems());
            items.forEach(item -> item.setOrder(savedOrder));
            List<OrderItemEntity> savedItems = itemRepository.saveAll(items);

            Order saved = orderMapper.toDomain(savedOrder);
            saved.setItems(itemMapper.toDomainList(savedItems));
            return saved;
        } catch (DataIntegrityViolationException exception) {
            throw new ValidationException(ExceptionMessages.ACTIVE_ORDER_EXISTS.getMessage());
        }
    }

    @Override
    public PageResult<Order> findByRestaurantIdAndStatus(
            Long restaurantId, OrderStatus status, int page, int size) {
        var orderPage = orderRepository.findByRestaurant_IdAndStatus(
                restaurantId, status, PageRequest.of(page, size, Sort.by("createdAt").ascending()));
        List<Long> orderIds = orderPage.getContent().stream()
                .map(OrderEntity::getId)
                .toList();
        Map<Long, List<OrderItemEntity>> itemsByOrder = findItemsByOrder(orderIds);
        List<Order> orders = orderPage.getContent().stream()
                .map(entity -> toOrder(entity, itemsByOrder.getOrDefault(entity.getId(), List.of())))
                .toList();
        return new PageResult<>(orders, orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages());
    }

    @Override
    public Optional<Order> assignPendingOrder(Long orderId, Long restaurantId, Long employeeId) {
        int updatedOrders = orderRepository.assignIfAvailable(orderId, restaurantId, employeeId,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION);
        if (updatedOrders == 0) {
            return Optional.empty();
        }
        return orderRepository.findById(orderId)
                .map(entity -> toOrder(entity, itemRepository.findByOrder_IdIn(List.of(orderId))));
    }

    @Override
    public Optional<Order> markOrderReady(Long orderId, Long restaurantId, Long employeeId, String securityPinHash) {
        int updatedOrders = orderRepository.markReadyIfAssigned(orderId, restaurantId, employeeId, securityPinHash,
                OrderStatus.IN_PREPARATION, OrderStatus.READY);
        if (updatedOrders == 0) {
            return Optional.empty();
        }
        return orderRepository.findById(orderId)
                .map(entity -> toOrder(entity, itemRepository.findByOrder_IdIn(List.of(orderId))));
    }

    @Override
    public Optional<Order> deliverReadyOrder(Long orderId, Long restaurantId, Long employeeId, String securityPinHash) {
        int updatedOrders = orderRepository.deliverIfReadyAndPinMatches(
                orderId, restaurantId, employeeId, securityPinHash, OrderStatus.READY, OrderStatus.DELIVERED);
        if (updatedOrders == 0) {
            return Optional.empty();
        }
        return orderRepository.findById(orderId).map(entity -> toOrder(entity, itemRepository.findByOrder_IdIn(List.of(orderId))));
    }

    private Map<Long, List<OrderItemEntity>> findItemsByOrder(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return itemRepository.findByOrder_IdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
    }

    private Order toOrder(OrderEntity entity, List<OrderItemEntity> items) {
        Order order = orderMapper.toDomain(entity);
        order.setItems(itemMapper.toDomainList(items));
        return order;
    }
}
