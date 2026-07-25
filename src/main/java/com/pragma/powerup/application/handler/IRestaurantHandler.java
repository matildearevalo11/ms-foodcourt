package com.pragma.powerup.application.handler;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;

public interface IRestaurantHandler {
    RestaurantResponseDto saveRestaurant(RestaurantRequestDto requestDto);
}
