package com.pragma.powerup.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.powerup.application.dto.request.OrderItemRequestDto;
import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.infrastructure.configuration.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderRestController.class)
@Import(SecurityConfiguration.class)
class OrderRestControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private IOrderHandler handler;

    @Test
    void createOrder_AsCustomer_ShouldReturnPendingOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto();
        response.setId(1L);
        response.setStatus(OrderStatus.PENDING);
        when(handler.saveOrder(any())).thenReturn(response);

        mockMvc.perform(post("/orders")
                        .with(customerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createOrder_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(customerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":5,\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.items").value("Order must contain at least one dish"));
    }

    @Test
    void createOrder_AsOwner_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(jwt().jwt(token -> token.claim("userId", 7L))
                                .authorities(new SimpleGrantedAuthority("ROLE_OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    private OrderRequestDto validRequest() {
        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setDishId(10L);
        item.setQuantity(2);
        OrderRequestDto request = new OrderRequestDto();
        request.setRestaurantId(5L);
        request.setItems(List.of(item));
        return request;
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerJwt() {
        return jwt().jwt(token -> token.claim("userId", 20L))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
}
