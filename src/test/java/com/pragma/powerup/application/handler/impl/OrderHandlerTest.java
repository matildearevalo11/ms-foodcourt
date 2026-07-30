package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderFilterRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

class OrderHandlerTest {
    @Test
    void saveOrder_ShouldMapDelegateAndMapResponse() {
        IOrderServicePort service = mock(IOrderServicePort.class);
        IOrderRequestMapper requestMapper = mock(IOrderRequestMapper.class);
        IOrderResponseMapper responseMapper = mock(IOrderResponseMapper.class);
        OrderHandler handler = new OrderHandler(service, requestMapper, responseMapper);
        OrderRequestDto request = new OrderRequestDto();
        Order order = new Order();
        Order saved = new Order();
        OrderResponseDto response = new OrderResponseDto();
        when(requestMapper.toOrder(request)).thenReturn(order);
        when(service.saveOrder(order)).thenReturn(saved);
        when(responseMapper.toResponse(saved)).thenReturn(response);

        assertSame(response, handler.saveOrder(request));
    }

    @Test
    void getOrders_ShouldMapAllOrdersAndPagination() {
        IOrderServicePort service = mock(IOrderServicePort.class);
        IOrderResponseMapper responseMapper = mock(IOrderResponseMapper.class);
        OrderHandler handler = new OrderHandler(service, mock(IOrderRequestMapper.class), responseMapper);
        OrderFilterRequestDto request = new OrderFilterRequestDto();
        request.setStatus(OrderStatus.PENDING);
        Order order = new Order();
        OrderResponseDto response = new OrderResponseDto();
        when(service.getOrders(OrderStatus.PENDING, 0, 10))
                .thenReturn(new PageResult<>(List.of(order), 0, 10, 1, 1));
        when(responseMapper.toResponseList(List.of(order))).thenReturn(List.of(response));

        var result = handler.getOrders(request);

        assertEquals(List.of(response), result.data());
        assertEquals(1, result.meta().totalElements());
    }
}
