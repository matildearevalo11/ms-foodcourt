package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.request.DishFilterRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.Dish;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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

    @Test
    void updateDishStatus_ShouldDelegateAndMapResponse() {
        IDishServicePort service = mock(IDishServicePort.class);
        IDishResponseMapper responseMapper = mock(IDishResponseMapper.class);
        DishHandler handler = new DishHandler(service, mock(IDishRequestMapper.class), responseMapper);
        DishStatusRequestDto request = new DishStatusRequestDto();
        request.setActive(false);
        Dish updated = new Dish();
        DishResponseDto response = new DishResponseDto();
        when(service.updateDishStatus(5L, 10L, false)).thenReturn(updated);
        when(responseMapper.toResponse(updated)).thenReturn(response);

        assertSame(response, handler.updateDishStatus(5L, 10L, request));
    }

    @Test
    void getDishes_ShouldMapContentAndMetadata() {
        IDishServicePort service = mock(IDishServicePort.class);
        IDishResponseMapper responseMapper = mock(IDishResponseMapper.class);
        DishHandler handler = new DishHandler(service, mock(IDishRequestMapper.class), responseMapper);
        DishFilterRequestDto request = new DishFilterRequestDto();
        request.setCategoryId(2L);
        Dish dish = new Dish();
        DishResponseDto response = new DishResponseDto();
        when(service.getDishes(5L, 2L, 0, 10))
                .thenReturn(new PageResult<>(List.of(dish), 0, 10, 1, 1));
        when(responseMapper.toResponseList(List.of(dish))).thenReturn(List.of(response));

        var result = handler.getDishes(5L, request);

        assertEquals(List.of(response), result.data());
        assertEquals(1, result.meta().totalElements());
    }
}
