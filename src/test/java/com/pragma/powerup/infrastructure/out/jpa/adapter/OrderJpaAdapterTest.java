package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
        Order order = new Order(null, 20L, 5L, OrderStatus.PENDING, null,
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
        Order order = new Order(null, 20L, 5L, OrderStatus.PENDING, null, List.of());
        OrderEntity entity = new OrderEntity();
        when(orderMapper.toEntity(order)).thenReturn(entity);
        when(orderRepository.saveAndFlush(entity)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> adapter().save(order)).isInstanceOf(ValidationException.class);
    }

    private OrderJpaAdapter adapter() {
        return new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
    }
}
