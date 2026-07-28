package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantSummaryResponseDto;
import com.pragma.powerup.application.mapper.IRestaurantSummaryResponseMapper;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.model.PageResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.List;

class RestaurantHandlerTest {
    @Test
    void saveRestaurant_ShouldMapDelegateAndMapResponse() {
        IRestaurantServicePort service = mock(IRestaurantServicePort.class);
        IRestaurantRequestMapper requestMapper = mock(IRestaurantRequestMapper.class);
        IRestaurantResponseMapper responseMapper = mock(IRestaurantResponseMapper.class);
        RestaurantHandler handler = new RestaurantHandler(service, requestMapper, responseMapper, mock(IRestaurantSummaryResponseMapper.class));
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

    @Test
    void getRestaurants_ShouldMapContentAndMetadata() {
        IRestaurantServicePort service = mock(IRestaurantServicePort.class);
        IRestaurantSummaryResponseMapper summaryMapper = mock(IRestaurantSummaryResponseMapper.class);
        RestaurantHandler handler = new RestaurantHandler(
                service, mock(IRestaurantRequestMapper.class), mock(IRestaurantResponseMapper.class), summaryMapper);
        List<Restaurant> restaurants = List.of(new Restaurant());
        List<RestaurantSummaryResponseDto> summaries = List.of(
                new RestaurantSummaryResponseDto("Burger", "logo.png"));
        when(service.getRestaurants(0, 10)).thenReturn(new PageResult<>(restaurants, 0, 10, 1, 1));
        when(summaryMapper.toResponseList(restaurants)).thenReturn(summaries);

        var result = handler.getRestaurants(0, 10);

        assertSame(summaries, result.data());
        assertEquals(1, result.meta().totalElements());
    }
}
