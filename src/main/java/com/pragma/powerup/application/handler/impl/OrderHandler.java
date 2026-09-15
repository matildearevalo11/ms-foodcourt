package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderHandler implements IOrderHandler {
    private final IOrderServicePort servicePort;
    private final IOrderRequestMapper requestMapper;
    private final IOrderResponseMapper responseMapper;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        return responseMapper.toResponse(servicePort.createOrder(requestMapper.toOrder(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponseDto> getOrdersByStatus(OrderStatus status, int page, int size) {
        var result = servicePort.getOrdersByStatus(status, page, size);
        return new PageResponseDto<>(responseMapper.toResponseList(result.content()),
                new PageMetadataDto(result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @Override
    @Transactional
    public OrderResponseDto assignOrder(Long orderId) {
        return responseMapper.toResponse(servicePort.assignOrder(orderId));
    }

    @Override
    @Transactional
    public OrderResponseDto markOrderReady(Long orderId) {
        return responseMapper.toResponse(servicePort.markOrderReady(orderId));
    }

    @Override
    @Transactional
    public OrderResponseDto deliverOrder(Long orderId, OrderDeliveryRequestDto request) {
        return responseMapper.toResponse(servicePort.deliverOrder(orderId, request.securityPin()));
    }
}
