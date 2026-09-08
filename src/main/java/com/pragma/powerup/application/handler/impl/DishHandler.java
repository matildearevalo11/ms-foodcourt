package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishHandler implements IDishHandler {
    private final IDishServicePort servicePort;
    private final IDishRequestMapper requestMapper;
    private final IDishResponseMapper responseMapper;

    @Override
    public DishResponseDto createDish(Long restaurantId, DishRequestDto request) {
        return responseMapper.toResponse(servicePort.createDish(requestMapper.toDish(request, restaurantId)));
    }
}
