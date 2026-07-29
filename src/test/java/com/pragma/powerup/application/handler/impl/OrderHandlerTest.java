package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

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
}
