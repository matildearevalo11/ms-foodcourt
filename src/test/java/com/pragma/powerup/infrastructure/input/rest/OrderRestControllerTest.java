package com.pragma.powerup.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.powerup.application.dto.request.OrderItemRequestDto;
import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.OrderItemResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Test
    void getOrders_AsEmployee_ShouldReturnFilteredOrdersWithAllFields() throws Exception {
        OrderItemResponseDto item = new OrderItemResponseDto();
        item.setDishId(10L);
        item.setQuantity(2);
        OrderResponseDto order = new OrderResponseDto();
        order.setId(25L);
        order.setCustomerId(20L);
        order.setRestaurantId(5L);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(java.time.Instant.parse("2026-07-29T15:00:00Z"));
        order.setItems(List.of(item));
        when(handler.getOrders(any())).thenReturn(new PageResponseDto<>(List.of(order),
                new PageMetadataDto(0, 5, 1, 1)));

        mockMvc.perform(get("/orders?status=PENDING&page=0&size=5")
                        .with(employeeJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(25))
                .andExpect(jsonPath("$.data[0].customerId").value(20))
                .andExpect(jsonPath("$.data[0].restaurantId").value(5))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].items[0].dishId").value(10))
                .andExpect(jsonPath("$.data[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$.meta.totalElements").value(1));
    }

    @Test
    void getOrders_WithoutStatus_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/orders").with(employeeJwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrders_AsCustomer_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/orders?status=PENDING").with(customerJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignOrder_AsEmployee_ShouldReturnOrderInPreparation() throws Exception {
        OrderResponseDto response = new OrderResponseDto();
        response.setId(25L);
        response.setAssignedEmployeeId(30L);
        response.setStatus(OrderStatus.IN_PREPARATION);
        when(handler.assignOrder(25L)).thenReturn(response);

        mockMvc.perform(patch("/orders/25/assignment").with(employeeJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(25))
                .andExpect(jsonPath("$.data.assignedEmployeeId").value(30))
                .andExpect(jsonPath("$.data.status").value("IN_PREPARATION"));
    }

    @Test
    void assignOrder_AsCustomer_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/orders/25/assignment").with(customerJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void markOrderReady_AsEmployee_ShouldReturnReadyOrder() throws Exception {
        OrderResponseDto response = new OrderResponseDto();
        response.setId(25L);
        response.setAssignedEmployeeId(30L);
        response.setStatus(OrderStatus.READY);
        when(handler.markOrderReady(25L)).thenReturn(response);

        mockMvc.perform(patch("/orders/25/ready").with(employeeJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(25))
                .andExpect(jsonPath("$.data.status").value("READY"));
    }

    @Test
    void markOrderReady_AsCustomer_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/orders/25/ready").with(customerJwt()))
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

    private org.springframework.test.web.servlet.request.RequestPostProcessor employeeJwt() {
        return jwt().jwt(token -> token.claim("userId", 30L).claim("restaurantId", 5L))
                .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }
}
