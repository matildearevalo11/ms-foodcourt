package com.pragma.powerup.application.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {
    @Mock
    IOrderServicePort servicePort;
    @Mock
    IOrderRequestMapper requestMapper;
    @Mock
    IOrderResponseMapper responseMapper;

    @Test
    void mapsPaginatedOrdersAndMetadata() {
        Order order = new Order();
        OrderResponseDto response = new OrderResponseDto(null, null, null, null, null, List.of());
        when(servicePort.getOrdersByStatus(OrderStatus.PENDING, 1, 5))
                .thenReturn(new PageResult<>(List.of(order), 1, 5, 6, 2));
        when(responseMapper.toResponseList(List.of(order))).thenReturn(List.of(response));

        var result = new OrderHandler(servicePort, requestMapper, responseMapper)
                .getOrdersByStatus(OrderStatus.PENDING, 1, 5);

        assertThat(result.data()).containsExactly(response);
        assertThat(result.meta().totalElements()).isEqualTo(6);
        assertThat(result.meta().totalPages()).isEqualTo(2);
    }
}
