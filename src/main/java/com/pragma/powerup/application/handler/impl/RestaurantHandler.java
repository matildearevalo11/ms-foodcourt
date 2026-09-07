package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantHandler implements IRestaurantHandler {
    private final IRestaurantServicePort servicePort;
    private final IRestaurantRequestMapper requestMapper;
    private final IRestaurantResponseMapper responseMapper;

    @Override
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto request) {
        return responseMapper.toResponse(servicePort.createRestaurant(requestMapper.toRestaurant(request)));
    }
}
