package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import java.util.Set;

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
}
