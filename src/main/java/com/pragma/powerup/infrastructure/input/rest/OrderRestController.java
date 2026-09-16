package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.OrderRequestDto;
import com.pragma.powerup.application.dto.request.OrderDeliveryRequestDto;
import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class OrderRestController {
    private final IOrderHandler handler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.CUSTOMER)
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto request) {
        return new DefaultResponse<>(handler.createOrder(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public PageResponseDto<OrderResponseDto> getOrdersByStatus(
            @RequestParam(required = false) @NotNull(message = "Order status is required") OrderStatus status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be zero or positive") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be positive")
            @Max(value = 100, message = "Size must be at most 100") int size) {
        return handler.getOrdersByStatus(status, page, size);
    }

    @PatchMapping(value = "/{orderId}/assignment", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public DefaultResponse<OrderResponseDto> assignOrder(@PathVariable Long orderId) {
        return new DefaultResponse<>(handler.assignOrder(orderId));
    }

    @PatchMapping(value = "/{orderId}/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public DefaultResponse<OrderResponseDto> markOrderReady(@PathVariable Long orderId) {
        return new DefaultResponse<>(handler.markOrderReady(orderId));
    }

    @PatchMapping(value = "/{orderId}/delivery", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.EMPLOYEE)
    public DefaultResponse<OrderResponseDto> deliverOrder(@PathVariable Long orderId, @Valid @RequestBody OrderDeliveryRequestDto request) {
        return new DefaultResponse<>(handler.deliverOrder(orderId, request));
    }

    @PatchMapping(value = "/{orderId}/cancellation", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.CUSTOMER)
    public DefaultResponse<OrderResponseDto> cancelOrder(@PathVariable Long orderId) {
        return new DefaultResponse<>(handler.cancelOrder(orderId));
    }
}
