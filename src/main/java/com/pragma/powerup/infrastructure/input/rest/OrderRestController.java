package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderFilterRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderRestController {
    private final IOrderHandler orderHandler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.CUSTOMER)
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto requestDto) {
        return new DefaultResponse<>(orderHandler.saveOrder(requestDto));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public PageResponseDto<OrderResponseDto> getOrders(@Valid @ModelAttribute OrderFilterRequestDto requestDto) {
        return orderHandler.getOrders(requestDto);
    }

    @PatchMapping(value = "/{orderId}/assignment", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public DefaultResponse<OrderResponseDto> assignOrder(@PathVariable Long orderId) {
        return new DefaultResponse<>(orderHandler.assignOrder(orderId));
    }

    @PatchMapping(value = "/{orderId}/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public DefaultResponse<OrderResponseDto> markOrderReady(@PathVariable Long orderId) {
        return new DefaultResponse<>(orderHandler.markOrderReady(orderId));
    }
}
