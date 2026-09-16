package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.domain.enums.OrderStatus;

public interface IOrderHandler {
    OrderResponseDto createOrder(OrderRequestDto request);

    PageResponseDto<OrderResponseDto> getOrdersByStatus(OrderStatus status, int page, int size);

    OrderResponseDto assignOrder(Long orderId);

    OrderResponseDto markOrderReady(Long orderId);

    OrderResponseDto deliverOrder(Long orderId, OrderDeliveryRequestDto request);

    OrderResponseDto cancelOrder(Long orderId);
}
