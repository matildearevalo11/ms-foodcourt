package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.dto.response.DishSummaryResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;

public interface IDishHandler {
    DishResponseDto createDish(Long restaurantId, DishRequestDto request);

    DishResponseDto updateDish(Long restaurantId, Long dishId, DishUpdateRequestDto request);

    DishResponseDto updateDishStatus(Long restaurantId, Long dishId, DishStatusRequestDto request);

    PageResponseDto<DishSummaryResponseDto> getDishes(Long restaurantId, Long categoryId, int page, int size);
}
