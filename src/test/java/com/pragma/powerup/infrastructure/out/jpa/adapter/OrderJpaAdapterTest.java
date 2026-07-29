package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderJpaAdapterTest {
    @Test
    void existsByCustomerIdAndStatusIn_ShouldDelegate() {
        IOrderRepository repository = mock(IOrderRepository.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(repository, mock(IOrderItemRepository.class),
                mock(IOrderEntityMapper.class), mock(IOrderItemEntityMapper.class));
        Set<OrderStatus> statuses = Set.of(OrderStatus.PENDING);
        when(repository.existsByCustomerIdAndStatusIn(20L, statuses)).thenReturn(true);

        assertTrue(adapter.existsByCustomerIdAndStatusIn(20L, statuses));
    }

    @Test
    void saveOrder_ShouldPersistOrderBeforeItsItems() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        IOrderItemRepository itemRepository = mock(IOrderItemRepository.class);
        IOrderEntityMapper orderMapper = mock(IOrderEntityMapper.class);
        IOrderItemEntityMapper itemMapper = mock(IOrderItemEntityMapper.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
        OrderItem orderItem = new OrderItem();
        Order order = new Order();
        order.setItems(List.of(orderItem));
        OrderEntity orderEntity = new OrderEntity();
        OrderItemEntity itemEntity = new OrderItemEntity();
        Order saved = new Order();
        when(orderMapper.toEntity(order)).thenReturn(orderEntity);
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);
        when(itemMapper.toEntities(order.getItems())).thenReturn(List.of(itemEntity));
        when(itemRepository.saveAll(List.of(itemEntity))).thenReturn(List.of(itemEntity));
        when(orderMapper.toOrder(orderEntity)).thenReturn(saved);
        when(itemMapper.toOrderItems(List.of(itemEntity))).thenReturn(List.of(orderItem));

        assertSame(saved, adapter.saveOrder(order));
        assertSame(orderEntity, itemEntity.getOrder());
        assertEquals(List.of(orderItem), saved.getItems());
    }
}
