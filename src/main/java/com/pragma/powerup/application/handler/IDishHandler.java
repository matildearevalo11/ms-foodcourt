package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;

public interface IDishHandler {
    DishResponseDto saveDish(Long restaurantId, DishRequestDto requestDto);
    DishResponseDto updateDish(Long restaurantId, Long dishId, DishUpdateRequestDto requestDto);
    DishResponseDto updateDishStatus(Long restaurantId, Long dishId, DishStatusRequestDto requestDto);
}
