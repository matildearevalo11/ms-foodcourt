package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.Restaurant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class RestaurantHandlerTest {
    @Test
    void saveRestaurant_ShouldMapDelegateAndMapResponse() {
        IRestaurantServicePort service = mock(IRestaurantServicePort.class);
        IRestaurantRequestMapper requestMapper = mock(IRestaurantRequestMapper.class);
        IRestaurantResponseMapper responseMapper = mock(IRestaurantResponseMapper.class);
        RestaurantHandler handler = new RestaurantHandler(service, requestMapper, responseMapper);
        RestaurantRequestDto request = new RestaurantRequestDto();
        Restaurant restaurant = new Restaurant();
        Restaurant saved = new Restaurant();
        RestaurantResponseDto expected = new RestaurantResponseDto();
        when(requestMapper.toRestaurant(request)).thenReturn(restaurant);
        when(service.saveRestaurant(restaurant)).thenReturn(saved);
        when(responseMapper.toResponse(saved)).thenReturn(expected);

        assertSame(expected, handler.saveRestaurant(request));
        verify(service).saveRestaurant(restaurant);
    }
}
