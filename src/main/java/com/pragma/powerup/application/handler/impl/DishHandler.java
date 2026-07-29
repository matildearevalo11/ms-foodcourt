package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishFilterRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
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

    @Override
    public DishResponseDto updateDishStatus(Long restaurantId, Long dishId, DishStatusRequestDto requestDto) {
        return responseMapper.toResponse(dishServicePort.updateDishStatus(restaurantId, dishId, requestDto.getActive()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<DishResponseDto> getDishes(Long restaurantId, DishFilterRequestDto requestDto) {
        var result = dishServicePort.getDishes(
                restaurantId, requestDto.getCategoryId(), requestDto.getPage(), requestDto.getSize());
        return new PageResponseDto<>(responseMapper.toResponseList(result.content()),
                new PageMetadataDto(result.page(), result.size(), result.totalElements(), result.totalPages()));
    }
}
