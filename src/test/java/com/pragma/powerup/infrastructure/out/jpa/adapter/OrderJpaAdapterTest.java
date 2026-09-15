package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class OrderJpaAdapterTest {
    @Mock
    IOrderRepository orderRepository;
    @Mock
    IOrderItemRepository itemRepository;
    @Mock
    IOrderEntityMapper orderMapper;
    @Mock
    IOrderItemEntityMapper itemMapper;

    @Test
    void savesOrderAndItsItems() {
        Order order = new Order(null, 20L, 5L, null, OrderStatus.PENDING, null,
                List.of(new OrderItem(null, 10L, 2)));
        OrderEntity entity = new OrderEntity();
        OrderItemEntity item = new OrderItemEntity();
        Order saved = new Order();
        when(orderMapper.toEntity(order)).thenReturn(entity);
        when(orderRepository.saveAndFlush(entity)).thenReturn(entity);
        when(itemMapper.toEntityList(order.getItems())).thenReturn(List.of(item));
        when(itemRepository.saveAll(List.of(item))).thenReturn(List.of(item));
        when(orderMapper.toDomain(entity)).thenReturn(saved);
        when(itemMapper.toDomainList(List.of(item))).thenReturn(order.getItems());
        when(orderRepository.existsByCustomerIdAndStatusIn(20L, OrderStatus.activeStatuses())).thenReturn(true);
        OrderJpaAdapter adapter = adapter();

        assertThat(adapter.save(order).getItems()).isEqualTo(order.getItems());
        assertThat(entity.getActiveOrder()).isTrue();
        assertThat(item.getOrder()).isSameAs(entity);
        assertThat(adapter.existsByCustomerIdAndStatusIn(20L, OrderStatus.activeStatuses())).isTrue();
    }

    @Test
    void translatesConcurrentActiveOrderConflict() {
        Order order = new Order(null, 20L, 5L, null, OrderStatus.PENDING, null, List.of());
        OrderEntity entity = new OrderEntity();
        when(orderMapper.toEntity(order)).thenReturn(entity);
        when(orderRepository.saveAndFlush(entity)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> adapter().save(order)).isInstanceOf(ValidationException.class);
    }

    @Test
    void loadsPaginatedRestaurantOrdersAndItemsInOneBatch() {
        OrderEntity entity = new OrderEntity();
        entity.setId(30L);
        OrderItemEntity item = new OrderItemEntity();
        item.setOrder(entity);
        Order order = new Order();
        OrderItem orderItem = new OrderItem(null, 10L, 2);
        when(orderRepository.findByRestaurant_IdAndStatus(eq(5L), eq(OrderStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 5), 1));
        when(itemRepository.findByOrder_IdIn(List.of(30L))).thenReturn(List.of(item));
        when(orderMapper.toDomain(entity)).thenReturn(order);
        when(itemMapper.toDomainList(List.of(item))).thenReturn(List.of(orderItem));

        var result = adapter().findByRestaurantIdAndStatus(5L, OrderStatus.PENDING, 0, 5);

        assertThat(result.content()).containsExactly(order);
        assertThat(result.content().getFirst().getItems()).containsExactly(orderItem);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void assignsOrderAtomicallyAndLoadsItsItems() {
        OrderEntity entity = new OrderEntity();
        entity.setId(30L);
        OrderItemEntity item = new OrderItemEntity();
        Order assignedOrder = new Order();
        when(orderRepository.assignIfAvailable(30L, 5L, 40L,
                OrderStatus.PENDING, OrderStatus.IN_PREPARATION)).thenReturn(1);
        when(orderRepository.findById(30L)).thenReturn(Optional.of(entity));
        when(itemRepository.findByOrder_IdIn(List.of(30L))).thenReturn(List.of(item));
        when(orderMapper.toDomain(entity)).thenReturn(assignedOrder);
        when(itemMapper.toDomainList(List.of(item))).thenReturn(List.of(new OrderItem(null, 10L, 2)));

        Optional<Order> result = adapter().assignPendingOrder(30L, 5L, 40L);

        assertThat(result).containsSame(assignedOrder);
        assertThat(assignedOrder.getItems()).hasSize(1);
    }

    @Test
    void doesNotLoadOrderWhenAssignmentIsUnavailable() {
        Optional<Order> result = adapter().assignPendingOrder(30L, 5L, 40L);

        assertThat(result).isEmpty();
    }

    @Test
    void marksAssignedOrderReadyAtomically() {
        OrderEntity entity = new OrderEntity();
        entity.setId(30L);
        Order readyOrder = new Order();
        when(orderRepository.markReadyIfAssigned(30L, 5L, 40L, "482913",
                OrderStatus.IN_PREPARATION, OrderStatus.READY)).thenReturn(1);
        when(orderRepository.findById(30L)).thenReturn(Optional.of(entity));
        when(itemRepository.findByOrder_IdIn(List.of(30L))).thenReturn(List.of());
        when(orderMapper.toDomain(entity)).thenReturn(readyOrder);
        when(itemMapper.toDomainList(List.of())).thenReturn(List.of());

        Optional<Order> result = adapter().markOrderReady(30L, 5L, 40L, "482913");

        assertThat(result).containsSame(readyOrder);
    }

    private OrderJpaAdapter adapter() {
        return new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
    }
}
