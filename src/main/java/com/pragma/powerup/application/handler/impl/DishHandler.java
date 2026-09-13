package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.dto.response.DishSummaryResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.application.mapper.IDishSummaryResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishHandler implements IDishHandler {
    private final IDishServicePort servicePort;
    private final IDishRequestMapper requestMapper;
    private final IDishResponseMapper responseMapper;
    private final IDishSummaryResponseMapper summaryResponseMapper;

    @Override
    public DishResponseDto createDish(Long restaurantId, DishRequestDto request) {
        return responseMapper.toResponse(servicePort.createDish(requestMapper.toDish(request, restaurantId)));
    }

    @Override
    public DishResponseDto updateDish(Long restaurantId, Long dishId, DishUpdateRequestDto request) {
        Dish dish = servicePort.updateDish(restaurantId, dishId, request.price(), request.description());
        return responseMapper.toResponse(dish);
    }

    @Override
    public DishResponseDto updateDishStatus(Long restaurantId, Long dishId, DishStatusRequestDto request) {
        Dish dish = servicePort.updateDishStatus(restaurantId, dishId, request.active());
        return responseMapper.toResponse(dish);
    }

    @Override
    public PageResponseDto<DishSummaryResponseDto> getDishes(Long restaurantId, Long categoryId, int page, int size) {
        PageResult<Dish> result = servicePort.getDishes(restaurantId, categoryId, page, size);
        PageMetadataDto metadata = new PageMetadataDto(
                result.page(), result.size(), result.totalElements(), result.totalPages());
        return new PageResponseDto<>(summaryResponseMapper.toResponseList(result.content()), metadata);
    }
}
