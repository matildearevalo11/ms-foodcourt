package com.pragma.powerup.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DishRestController.class)
class DishRestControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IDishHandler handler;

    @Test
    void createDish_WithValidRequest_ShouldReturnCreatedAndActive() throws Exception {
        DishRequestDto request = validRequest();
        DishResponseDto response = new DishResponseDto();
        response.setId(1L);
        response.setName(request.getName());
        response.setRestaurantId(5L);
        response.setActive(true);
        when(handler.saveDish(eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/restaurants/5/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void createDish_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/restaurants/5/dishes")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Dish name is required"))
                .andExpect(jsonPath("$.errors.price").value("Dish price is required"));
    }

    @Test
    void createDish_WithZeroPrice_ShouldReturnBadRequest() throws Exception {
        DishRequestDto request = validRequest();
        request.setPrice(0L);
        mockMvc.perform(post("/restaurants/5/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("Dish price must be greater than zero"));
    }

    private DishRequestDto validRequest() {
        DishRequestDto request = new DishRequestDto();
        request.setName("Hamburguesa");
        request.setPrice(25000L);
        request.setDescription("Carne y queso");
        request.setUrlImage("https://cdn.example.com/dish.png");
        request.setCategoryId(2L);
        return request;
    }
}
