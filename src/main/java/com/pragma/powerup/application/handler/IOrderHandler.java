package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.domain.enums.OrderStatus;

public interface IOrderHandler {
    OrderResponseDto createOrder(OrderRequestDto request);

    PageResponseDto<OrderResponseDto> getOrdersByStatus(OrderStatus status, int page, int size);
}
