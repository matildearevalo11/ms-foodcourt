package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/restaurants/{restaurantId}/dishes")
@RequiredArgsConstructor
public class DishRestController {
    private final IDishHandler handler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.OWNER)
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<DishResponseDto> createDish(@PathVariable @Positive Long restaurantId,
                                                       @Valid @RequestBody DishRequestDto request) {
        return new DefaultResponse<>(handler.createDish(restaurantId, request));
    }

    @PatchMapping(value = "/{dishId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.OWNER)
    public DefaultResponse<DishResponseDto> updateDish(@PathVariable @Positive Long restaurantId,
            @PathVariable @Positive Long dishId, @Valid @RequestBody DishUpdateRequestDto request) {
        return new DefaultResponse<>(handler.updateDish(restaurantId, dishId, request));
    }

    @PatchMapping(value = "/{dishId}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.OWNER)
    public DefaultResponse<DishResponseDto> updateDishStatus(@PathVariable @Positive Long restaurantId,
            @PathVariable @Positive Long dishId, @Valid @RequestBody DishStatusRequestDto request) {
        return new DefaultResponse<>(handler.updateDishStatus(restaurantId, dishId, request));
    }
}
