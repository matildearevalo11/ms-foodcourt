package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.application.mapper.IOrderRequestMapper;
import com.pragma.powerup.application.mapper.IOrderResponseMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
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
}
