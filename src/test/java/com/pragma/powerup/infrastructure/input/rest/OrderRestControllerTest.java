package com.pragma.powerup.infrastructure.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.pragma.powerup.application.dto.response.OrderItemResponseDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.infrastructure.configuration.SecurityConfiguration;
import com.pragma.powerup.infrastructure.exceptionhandler.ControllerAdvisor;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderRestController.class)
@Import({ControllerAdvisor.class, SecurityConfiguration.class})
class OrderRestControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    IOrderHandler handler;

    @Test
    void createsOrderAsCustomer() throws Exception {
        when(handler.createOrder(any())).thenReturn(new OrderResponseDto(
                30L, 20L, 5L, null, OrderStatus.PENDING, Instant.parse("2026-09-13T12:00:00Z"),
                List.of(new OrderItemResponseDto(10L, 2))));

        mvc.perform(post("/orders")
                        .with(customerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    void validatesRequestAndRequiresCustomerRole() throws Exception {
        mvc.perform(post("/orders")
                        .with(customerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restaurantId\":5,\"items\":[]}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OWNER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void listsPaginatedOrdersWithAllFieldsAsEmployee() throws Exception {
        OrderResponseDto order = new OrderResponseDto(
                30L, 20L, 5L, null, OrderStatus.PENDING, Instant.parse("2026-09-13T12:00:00Z"),
                List.of(new OrderItemResponseDto(10L, 2)));
        when(handler.getOrdersByStatus(OrderStatus.PENDING, 1, 5)).thenReturn(
                new PageResponseDto<>(List.of(order), new PageMetadataDto(1, 5, 6, 2)));

        mvc.perform(get("/orders")
                        .with(employeeJwt())
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(30))
                .andExpect(jsonPath("$.data[0].customerId").value(20))
                .andExpect(jsonPath("$.data[0].restaurantId").value(5))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[0].createdAt").exists())
                .andExpect(jsonPath("$.data[0].items[0].dishId").value(10))
                .andExpect(jsonPath("$.data[0].items[0].quantity").value(2))
                .andExpect(jsonPath("$.meta.totalElements").value(6));
    }

    @Test
    void rejectsInvalidFilterAndNonEmployeeRole() throws Exception {
        mvc.perform(get("/orders").with(employeeJwt()).param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").value("Request parameter has an invalid format"));

        mvc.perform(get("/orders").with(employeeJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").value(org.hamcrest.Matchers.containsString(
                        "Order status is required")));

        mvc.perform(get("/orders").with(customerJwt()).param("status", "PENDING"))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignsOrderAsEmployee() throws Exception {
        when(handler.assignOrder(30L)).thenReturn(new OrderResponseDto(
                30L, 20L, 5L, 40L, OrderStatus.IN_PREPARATION,
                Instant.parse("2026-09-13T12:00:00Z"), List.of(new OrderItemResponseDto(10L, 2))));

        mvc.perform(patch("/orders/30/assignment").with(employeeJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedEmployeeId").value(40))
                .andExpect(jsonPath("$.data.status").value("IN_PREPARATION"));
    }

    @Test
    void rejectsAssignmentFromNonEmployee() throws Exception {
        mvc.perform(patch("/orders/30/assignment").with(customerJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void marksOrderReadyAsEmployee() throws Exception {
        when(handler.markOrderReady(30L)).thenReturn(new OrderResponseDto(
                30L, 20L, 5L, 40L, OrderStatus.READY,
                Instant.parse("2026-09-13T12:00:00Z"), List.of(new OrderItemResponseDto(10L, 2))));

        mvc.perform(patch("/orders/30/ready").with(employeeJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"));
    }

    @Test
    void rejectsReadyTransitionFromNonEmployee() throws Exception {
        mvc.perform(patch("/orders/30/ready").with(customerJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deliversOrderWithValidPinAsEmployee() throws Exception {
        when(handler.deliverOrder(org.mockito.ArgumentMatchers.eq(30L), any())).thenReturn(
                new OrderResponseDto(30L, 20L, 5L, 40L, OrderStatus.DELIVERED,
                        Instant.parse("2026-09-13T12:00:00Z"),
                        List.of(new OrderItemResponseDto(10L, 2))));

        mvc.perform(patch("/orders/30/delivery")
                        .with(employeeJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"securityPin\":\"482913\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DELIVERED"));
    }

    @Test
    void validatesDeliveryPinAndRequiresEmployeeRole() throws Exception {
        mvc.perform(patch("/orders/30/delivery")
                        .with(employeeJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"securityPin\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.securityPin").value("Security PIN must contain 6 digits"));

        mvc.perform(patch("/orders/30/delivery")
                        .with(customerJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"securityPin\":\"482913\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelsPendingOrderAsCustomer() throws Exception {
        when(handler.cancelOrder(30L)).thenReturn(
                new OrderResponseDto(30L, 20L, 5L, null, OrderStatus.CANCELED,
                        Instant.parse("2026-09-13T12:00:00Z"),
                        List.of(new OrderItemResponseDto(10L, 2))));

        mvc.perform(patch("/orders/30/cancellation").with(customerJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        mvc.perform(patch("/orders/30/cancellation").with(employeeJwt()))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerJwt() {
        return jwt().jwt(token -> token.subject("20").claim("role", "CUSTOMER"))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor employeeJwt() {
        return jwt().jwt(token -> token.subject("30")
                        .claim("role", "EMPLOYEE")
                        .claim("restaurantId", 5L))
                .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    private String validBody() {
        return """
                {"restaurantId":5,"items":[{"dishId":10,"quantity":2}]}
                """;
    }
}
