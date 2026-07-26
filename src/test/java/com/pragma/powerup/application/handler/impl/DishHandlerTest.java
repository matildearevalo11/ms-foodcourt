package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.Dish;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class DishHandlerTest {
    @Test
    void saveDish_ShouldMapDelegateAndMapResponse() {
        IDishServicePort service = mock(IDishServicePort.class);
        IDishRequestMapper requestMapper = mock(IDishRequestMapper.class);
        IDishResponseMapper responseMapper = mock(IDishResponseMapper.class);
        DishHandler handler = new DishHandler(service, requestMapper, responseMapper);
        DishRequestDto request = new DishRequestDto();
        Dish dish = new Dish();
        Dish saved = new Dish();
        DishResponseDto response = new DishResponseDto();
        when(requestMapper.toDish(request, 5L)).thenReturn(dish);
        when(service.saveDish(dish)).thenReturn(saved);
        when(responseMapper.toResponse(saved)).thenReturn(response);

        assertSame(response, handler.saveDish(5L, request));
        verify(service).saveDish(dish);
    }

    @Test
    void updateDish_ShouldDelegateAndMapResponse() {
        IDishServicePort service = mock(IDishServicePort.class);
        IDishResponseMapper responseMapper = mock(IDishResponseMapper.class);
        DishHandler handler = new DishHandler(service, mock(IDishRequestMapper.class), responseMapper);
        DishUpdateRequestDto request = new DishUpdateRequestDto();
        request.setPrice(30000L);
        request.setDescription("Nueva descripción");
        Dish updated = new Dish();
        DishResponseDto response = new DishResponseDto();
        when(service.updateDish(5L, 10L, 30000L, "Nueva descripción")).thenReturn(updated);
        when(responseMapper.toResponse(updated)).thenReturn(response);

        assertSame(response, handler.updateDish(5L, 10L, request));
    }
}
