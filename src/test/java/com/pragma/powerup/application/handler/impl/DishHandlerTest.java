package com.pragma.powerup.application.handler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.mapper.IDishRequestMapper;
import com.pragma.powerup.application.mapper.IDishResponseMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.Dish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DishHandlerTest {
    @Mock
    IDishServicePort servicePort;

    @Mock
    IDishRequestMapper requestMapper;

    @Mock
    IDishResponseMapper responseMapper;

    @Test
    void delegatesDishCreationThroughApplicationPorts() {
        DishRequestDto request = new DishRequestDto(
                "Hamburguesa", 25000L, "Carne y queso", "https://cdn.example.com/dish.png", 2L);
        Dish dish = new Dish();
        DishResponseDto expected = new DishResponseDto(
                1L, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, true);
        when(requestMapper.toDish(request, 5L)).thenReturn(dish);
        when(servicePort.createDish(dish)).thenReturn(dish);
        when(responseMapper.toResponse(dish)).thenReturn(expected);

        DishHandler handler = new DishHandler(servicePort, requestMapper, responseMapper);

        assertThat(handler.createDish(5L, request)).isEqualTo(expected);
    }
}
