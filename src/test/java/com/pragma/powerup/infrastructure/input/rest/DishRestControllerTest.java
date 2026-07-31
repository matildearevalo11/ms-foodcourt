package com.pragma.powerup.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import com.pragma.powerup.infrastructure.configuration.SecurityConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;

@WebMvcTest(DishRestController.class)
@Import(SecurityConfiguration.class)
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
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void createDish_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/restaurants/5/dishes")
                        .with(ownerJwt())
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
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("Dish price must be greater than zero"));
    }

    @Test
    void updateDish_WithValidRequest_ShouldReturnUpdatedDish() throws Exception {
        DishUpdateRequestDto request = validUpdateRequest();
        DishResponseDto response = new DishResponseDto();
        response.setId(10L);
        response.setPrice(request.getPrice());
        response.setDescription(request.getDescription());
        when(handler.updateDish(eq(5L), eq(10L), any())).thenReturn(response);

        mockMvc.perform(patch("/restaurants/5/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.price").value(30000))
                .andExpect(jsonPath("$.data.description").value("Nueva descripción"));
    }

    @Test
    void updateDish_WithFieldsOutsideContract_ShouldIgnoreThem() throws Exception {
        DishResponseDto response = new DishResponseDto();
        response.setId(10L);
        response.setPrice(30000L);
        response.setDescription("Nueva descripción");
        when(handler.updateDish(eq(5L), eq(10L), any())).thenReturn(response);

        mockMvc.perform(patch("/restaurants/5/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":30000,\"description\":\"Nueva descripción\",\"name\":\"No permitido\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateDish_WithInvalidFields_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/restaurants/5/dishes/10")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":0,\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").value("Dish price must be greater than zero"))
                .andExpect(jsonPath("$.errors.description").value("Description is required"));
    }

    @Test
    void createDish_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/restaurants/5/dishes").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errors.code").value("SEC-001"));
    }

    @Test
    void createDish_AsEmployee_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/restaurants/5/dishes")
                        .with(jwt().jwt(token -> token.subject("12"))
                                .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateDishStatus_WithValidRequest_ShouldReturnUpdatedStatus() throws Exception {
        DishResponseDto response = new DishResponseDto();
        response.setId(10L);
        response.setActive(false);
        when(handler.updateDishStatus(eq(5L), eq(10L), any())).thenReturn(response);

        mockMvc.perform(patch("/restaurants/5/dishes/10/status")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void updateDishStatus_WithoutStatus_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/restaurants/5/dishes/10/status")
                        .with(ownerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.active").value("Dish status is required"));
    }

    @Test
    void getDishes_AsCustomer_ShouldReturnFilteredPage() throws Exception {
        DishResponseDto dish = new DishResponseDto();
        dish.setId(10L);
        dish.setName("Hamburguesa");
        dish.setActive(true);
        when(handler.getDishes(eq(5L), any())).thenReturn(
                new PageResponseDto<>(List.of(dish), new PageMetadataDto(0, 5, 1, 1)));

        mockMvc.perform(get("/restaurants/5/dishes")
                        .with(customerJwt())
                        .param("categoryId", "2")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Hamburguesa"))
                .andExpect(jsonPath("$.meta.totalElements").value(1));
    }

    @Test
    void getDishes_AsOwner_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/restaurants/5/dishes").with(ownerJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDishes_WithInvalidPagination_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/restaurants/5/dishes")
                        .with(customerJwt())
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
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

    private DishUpdateRequestDto validUpdateRequest() {
        DishUpdateRequestDto request = new DishUpdateRequestDto();
        request.setPrice(30000L);
        request.setDescription("Nueva descripción");
        return request;
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor ownerJwt() {
        return jwt().jwt(token -> token.subject("7"))
                .authorities(new SimpleGrantedAuthority("ROLE_OWNER"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerJwt() {
        return jwt().jwt(token -> token.subject("20"))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
}
