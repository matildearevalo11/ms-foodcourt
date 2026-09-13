package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantSummaryResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
@Validated
public class RestaurantRestController {
    private final IRestaurantHandler handler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.ADMIN)
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<RestaurantResponseDto> createRestaurant(@Valid @RequestBody RestaurantRequestDto request) {
        return new DefaultResponse<>(handler.createRestaurant(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.CUSTOMER)
    public PageResponseDto<RestaurantSummaryResponseDto> getRestaurants(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        return handler.getRestaurants(page, size);
    }

    @GetMapping("/{restaurantId}/ownership")
    @RequireRole(RoleEnum.OWNER)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateOwnership(@PathVariable @Positive Long restaurantId) {
        handler.validateOwnership(restaurantId);
    }
}
