package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.DishRequestDto;
import com.pragma.powerup.application.dto.request.DishUpdateRequestDto;
import com.pragma.powerup.application.dto.request.DishStatusRequestDto;
import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.RequireRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/restaurants/{restaurantId}/dishes")
@RequiredArgsConstructor
public class DishRestController {
    private final IDishHandler dishHandler;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.OWNER)
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultResponse<DishResponseDto> createDish(@PathVariable Long restaurantId, @Valid @RequestBody DishRequestDto requestDto) {
        return new DefaultResponse<>(dishHandler.saveDish(restaurantId, requestDto));
    }

    @PatchMapping(value = "/{dishId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.OWNER)
    public DefaultResponse<DishResponseDto> updateDish(@PathVariable Long restaurantId, @PathVariable Long dishId,
                                                       @Valid @RequestBody DishUpdateRequestDto requestDto) {
        return new DefaultResponse<>(dishHandler.updateDish(restaurantId, dishId, requestDto));
    }

    @PatchMapping(value = "/{dishId}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequireRole(RoleEnum.OWNER)
    public DefaultResponse<DishResponseDto> updateDishStatus(@PathVariable Long restaurantId, @PathVariable Long dishId,
            @Valid @RequestBody DishStatusRequestDto requestDto) {
        return new DefaultResponse<>(dishHandler.updateDishStatus(restaurantId, dishId, requestDto));
    }
}
