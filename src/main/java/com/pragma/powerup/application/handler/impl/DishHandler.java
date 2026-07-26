package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DishHandler implements IDishHandler {
    private final IDishServicePort dishServicePort;
    private final IDishRequestMapper requestMapper;
    private final IDishResponseMapper responseMapper;

    @Override
    public DishResponseDto saveDish(Long restaurantId, DishRequestDto requestDto) {
        return responseMapper.toResponse(dishServicePort.saveDish(requestMapper.toDish(requestDto, restaurantId)));
    }

    @Override
    public DishResponseDto updateDish(Long restaurantId, Long dishId, DishUpdateRequestDto requestDto) {
        return responseMapper.toResponse(dishServicePort.updateDish(
                restaurantId, dishId, requestDto.getPrice(), requestDto.getDescription()));
    }
}
