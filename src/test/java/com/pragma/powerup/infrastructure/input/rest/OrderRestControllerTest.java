package com.pragma.powerup.infrastructure.input.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.pragma.powerup.application.dto.response.OrderItemResponseDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
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
                30L, 20L, 5L, OrderStatus.PENDING, Instant.parse("2026-09-13T12:00:00Z"),
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

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerJwt() {
        return jwt().jwt(token -> token.subject("20").claim("role", "CUSTOMER"))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private String validBody() {
        return """
                {"restaurantId":5,"items":[{"dishId":10,"quantity":2}]}
                """;
    }
}
