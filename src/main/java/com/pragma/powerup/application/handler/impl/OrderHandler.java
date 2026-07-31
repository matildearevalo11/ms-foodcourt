package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderFilterRequestDto;
import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderHandler implements IOrderHandler {
    private final IOrderServicePort orderServicePort;
    private final IOrderRequestMapper requestMapper;
    private final IOrderResponseMapper responseMapper;

    @Override
    public OrderResponseDto saveOrder(OrderRequestDto requestDto) {
        return responseMapper.toResponse(orderServicePort.saveOrder(requestMapper.toOrder(requestDto)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<OrderResponseDto> getOrders(OrderFilterRequestDto requestDto) {
        var result = orderServicePort.getOrders(requestDto.getStatus(), requestDto.getPage(), requestDto.getSize());
        return new PageResponseDto<>(responseMapper.toResponseList(result.content()),
                new PageMetadataDto(result.page(), result.size(), result.totalElements(), result.totalPages()));
    }

    @Override
    public OrderResponseDto assignOrder(Long orderId) {
        return responseMapper.toResponse(orderServicePort.assignOrder(orderId));
    }

    @Override
    public OrderResponseDto markOrderReady(Long orderId) {
        return responseMapper.toResponse(orderServicePort.markOrderReady(orderId));
    }

    @Override
    public OrderResponseDto deliverOrder(Long orderId, OrderDeliveryRequestDto requestDto) {
        return responseMapper.toResponse(orderServicePort.deliverOrder(orderId, requestDto.getSecurityPin()));
    }

    @Override
    public OrderResponseDto cancelOrder(Long orderId) {
        return responseMapper.toResponse(orderServicePort.cancelOrder(orderId));
    }
}
