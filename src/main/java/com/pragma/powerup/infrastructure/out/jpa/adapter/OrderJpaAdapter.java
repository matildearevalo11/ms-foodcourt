package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

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
}
