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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

    @Test
    void findByRestaurantIdAndStatus_ShouldReturnOrdersWithItems() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        IOrderItemRepository itemRepository = mock(IOrderItemRepository.class);
        IOrderEntityMapper orderMapper = mock(IOrderEntityMapper.class);
        IOrderItemEntityMapper itemMapper = mock(IOrderItemEntityMapper.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
        OrderEntity entity = new OrderEntity();
        entity.setId(25L);
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrder(entity);
        Order order = new Order();
        OrderItem item = new OrderItem();
        when(orderRepository.findByRestaurant_IdAndStatus(eq(5L), eq(OrderStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));
        when(itemRepository.findByOrder_IdIn(List.of(25L))).thenReturn(List.of(itemEntity));
        when(orderMapper.toOrder(entity)).thenReturn(order);
        when(itemMapper.toOrderItems(List.of(itemEntity))).thenReturn(List.of(item));

        var result = adapter.findByRestaurantIdAndStatus(5L, OrderStatus.PENDING, 0, 10);

        assertEquals(List.of(order), result.content());
        assertEquals(List.of(item), order.getItems());
    }

    @Test
    void assignPendingOrder_WhenAvailable_ShouldReturnUpdatedOrderWithItems() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        IOrderItemRepository itemRepository = mock(IOrderItemRepository.class);
        IOrderEntityMapper orderMapper = mock(IOrderEntityMapper.class);
        IOrderItemEntityMapper itemMapper = mock(IOrderItemEntityMapper.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
        OrderEntity entity = new OrderEntity();
        entity.setId(25L);
        Order order = new Order();
        OrderItemEntity itemEntity = new OrderItemEntity();
        OrderItem item = new OrderItem();
        when(orderRepository.assignIfAvailable(25L, 5L, 30L,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION)).thenReturn(1);
        when(orderRepository.findById(25L)).thenReturn(java.util.Optional.of(entity));
        when(itemRepository.findByOrder_IdIn(List.of(25L))).thenReturn(List.of(itemEntity));
        when(orderMapper.toOrder(entity)).thenReturn(order);
        when(itemMapper.toOrderItems(List.of(itemEntity))).thenReturn(List.of(item));

        var result = adapter.assignPendingOrder(25L, 5L, 30L);

        assertTrue(result.isPresent());
        assertEquals(List.of(item), result.orElseThrow().getItems());
    }

    @Test
    void assignPendingOrder_WhenUnavailable_ShouldNotLoadOrder() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, mock(IOrderItemRepository.class),
                mock(IOrderEntityMapper.class), mock(IOrderItemEntityMapper.class));

        assertTrue(adapter.assignPendingOrder(25L, 5L, 30L).isEmpty());
        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    void markOrderReady_WhenAssigned_ShouldReturnUpdatedOrder() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        IOrderItemRepository itemRepository = mock(IOrderItemRepository.class);
        IOrderEntityMapper orderMapper = mock(IOrderEntityMapper.class);
        IOrderItemEntityMapper itemMapper = mock(IOrderItemEntityMapper.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
        OrderEntity entity = new OrderEntity();
        entity.setId(25L);
        Order order = new Order();
        when(orderRepository.markReadyIfAssigned(25L, 5L, 30L, "482913",
                OrderStatus.IN_PREPARATION, OrderStatus.READY)).thenReturn(1);
        when(orderRepository.findById(25L)).thenReturn(java.util.Optional.of(entity));
        when(itemRepository.findByOrder_IdIn(List.of(25L))).thenReturn(List.of());
        when(orderMapper.toOrder(entity)).thenReturn(order);
        when(itemMapper.toOrderItems(List.of())).thenReturn(List.of());

        assertTrue(adapter.markOrderReady(25L, 5L, 30L, "482913").isPresent());
    }

    @Test
    void markOrderReady_WhenUnavailable_ShouldReturnEmpty() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, mock(IOrderItemRepository.class),
                mock(IOrderEntityMapper.class), mock(IOrderItemEntityMapper.class));

        assertTrue(adapter.markOrderReady(25L, 5L, 30L, "482913").isEmpty());
    }

    @Test
    void deliverReadyOrder_WhenPinMatches_ShouldReturnDeliveredOrder() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        IOrderItemRepository itemRepository = mock(IOrderItemRepository.class);
        IOrderEntityMapper orderMapper = mock(IOrderEntityMapper.class);
        IOrderItemEntityMapper itemMapper = mock(IOrderItemEntityMapper.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
        OrderEntity entity = new OrderEntity();
        entity.setId(25L);
        Order order = new Order();
        when(orderRepository.deliverIfReadyAndPinMatches(25L, 5L, 30L, "482913",
                OrderStatus.READY, OrderStatus.DELIVERED)).thenReturn(1);
        when(orderRepository.findById(25L)).thenReturn(java.util.Optional.of(entity));
        when(itemRepository.findByOrder_IdIn(List.of(25L))).thenReturn(List.of());
        when(orderMapper.toOrder(entity)).thenReturn(order);
        when(itemMapper.toOrderItems(List.of())).thenReturn(List.of());

        assertTrue(adapter.deliverReadyOrder(25L, 5L, 30L, "482913").isPresent());
    }

    @Test
    void deliverReadyOrder_WhenPinDoesNotMatch_ShouldReturnEmpty() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, mock(IOrderItemRepository.class),
                mock(IOrderEntityMapper.class), mock(IOrderItemEntityMapper.class));

        assertTrue(adapter.deliverReadyOrder(25L, 5L, 30L, "000000").isEmpty());
        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    void cancelPendingOrder_WhenOwnedByCustomer_ShouldReturnCanceledOrder() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        IOrderItemRepository itemRepository = mock(IOrderItemRepository.class);
        IOrderEntityMapper orderMapper = mock(IOrderEntityMapper.class);
        IOrderItemEntityMapper itemMapper = mock(IOrderItemEntityMapper.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
        OrderEntity entity = new OrderEntity();
        entity.setId(25L);
        Order order = new Order();
        when(orderRepository.cancelIfPending(25L, 20L, OrderStatus.PENDING, OrderStatus.CANCELED))
                .thenReturn(1);
        when(orderRepository.findById(25L)).thenReturn(java.util.Optional.of(entity));
        when(itemRepository.findByOrder_IdIn(List.of(25L))).thenReturn(List.of());
        when(orderMapper.toOrder(entity)).thenReturn(order);
        when(itemMapper.toOrderItems(List.of())).thenReturn(List.of());

        assertTrue(adapter.cancelPendingOrder(25L, 20L).isPresent());
    }

    @Test
    void cancelPendingOrder_WhenUnavailable_ShouldReturnEmpty() {
        IOrderRepository orderRepository = mock(IOrderRepository.class);
        OrderJpaAdapter adapter = new OrderJpaAdapter(orderRepository, mock(IOrderItemRepository.class),
                mock(IOrderEntityMapper.class), mock(IOrderItemEntityMapper.class));

        assertTrue(adapter.cancelPendingOrder(25L, 20L).isEmpty());
        verify(orderRepository, never()).findById(anyLong());
    }
}
