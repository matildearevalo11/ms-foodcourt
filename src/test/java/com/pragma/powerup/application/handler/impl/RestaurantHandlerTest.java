package com.pragma.powerup.application.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.Restaurant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantHandlerTest {
    @Mock
    IRestaurantServicePort servicePort;

    @Mock
    IRestaurantRequestMapper requestMapper;

    @Mock
    IRestaurantResponseMapper responseMapper;

    @Test
    void delegatesCreationThroughApplicationPorts() {
        RestaurantRequestDto request = new RestaurantRequestDto(
                "Restaurante", "9001", "Local 1", "3001234567", "https://logo.test/a.png", 7L);
        Restaurant restaurant = new Restaurant();
        RestaurantResponseDto expected = new RestaurantResponseDto(
                1L, "Restaurante", "9001", "Local 1", "3001234567", "https://logo.test/a.png", 7L);
        when(requestMapper.toRestaurant(request)).thenReturn(restaurant);
        when(servicePort.createRestaurant(restaurant)).thenReturn(restaurant);
        when(responseMapper.toResponse(restaurant)).thenReturn(expected);

        RestaurantHandler handler = new RestaurantHandler(servicePort, requestMapper, responseMapper);

        assertThat(handler.createRestaurant(request)).isEqualTo(expected);
    }
}
