package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderFilterRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;

public interface IOrderHandler {

    OrderResponseDto saveOrder(OrderRequestDto requestDto);
    PageResponseDto<OrderResponseDto> getOrders(OrderFilterRequestDto requestDto);
}
