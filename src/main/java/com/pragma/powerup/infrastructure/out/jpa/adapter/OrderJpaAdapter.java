package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderJpaAdapter implements IOrderPersistencePort {
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IOrderEntityMapper orderMapper;
    private final IOrderItemEntityMapper orderItemMapper;

    @Override
    public boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses) {
        return orderRepository.existsByCustomerIdAndStatusIn(customerId, statuses);
    }

    @Override
    public Order saveOrder(Order order) {
        OrderEntity savedOrder = orderRepository.save(orderMapper.toEntity(order));
        var itemEntities = orderItemMapper.toEntities(order.getItems());
        itemEntities.forEach(item -> item.setOrder(savedOrder));
        var savedItems = orderItemRepository.saveAll(itemEntities);
        Order result = orderMapper.toOrder(savedOrder);
        result.setItems(orderItemMapper.toOrderItems(savedItems));
        return result;
    }

    @Override
    public PageResult<Order> findByRestaurantIdAndStatus(
            Long restaurantId, OrderStatus status, int page, int size) {
        var orderPage = orderRepository.findByRestaurant_IdAndStatus(restaurantId, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")));
        List<Long> orderIds = orderPage.getContent().stream().map(OrderEntity::getId).toList();
        Map<Long, List<OrderItemEntity>> itemsByOrder = orderIds.isEmpty()
                ? Map.of()
                : orderItemRepository.findByOrder_IdIn(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
        List<Order> orders = orderPage.getContent().stream().map(entity -> {
            Order order = orderMapper.toOrder(entity);
            order.setItems(orderItemMapper.toOrderItems(itemsByOrder.getOrDefault(entity.getId(), List.of())));
            return order;
        }).toList();
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
        return findOrderWithItems(orderId);
    }

    @Override
    public Optional<Order> markOrderReady(Long orderId, Long restaurantId, Long employeeId, String securityPin) {
        int updatedOrders = orderRepository.markReadyIfAssigned(orderId, restaurantId, employeeId, securityPin,
                OrderStatus.IN_PREPARATION, OrderStatus.READY);
        if (updatedOrders == 0) {
            return Optional.empty();
        }
        return findOrderWithItems(orderId);
    }

    @Override
    public Optional<Order> deliverReadyOrder(Long orderId, Long restaurantId, Long employeeId, String securityPin) {
        int updatedOrders = orderRepository.deliverIfReadyAndPinMatches(orderId, restaurantId, employeeId, securityPin,
                OrderStatus.READY, OrderStatus.DELIVERED);
        if (updatedOrders == 0) {
            return Optional.empty();
        }
        return findOrderWithItems(orderId);
    }

    private Optional<Order> findOrderWithItems(Long orderId) {
        return orderRepository.findById(orderId).map(entity -> {
            Order order = orderMapper.toOrder(entity);
            order.setItems(orderItemMapper.toOrderItems(orderItemRepository.findByOrder_IdIn(List.of(orderId))));
            return order;
        });
    }
}
