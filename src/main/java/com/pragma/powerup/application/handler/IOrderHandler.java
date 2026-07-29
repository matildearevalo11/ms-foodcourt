package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;

public interface IOrderHandler {

    OrderResponseDto saveOrder(OrderRequestDto requestDto);
}
