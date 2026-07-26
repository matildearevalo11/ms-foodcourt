package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.domain.model.Dish;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IDishResponseMapper {
    DishResponseDto toResponse(Dish dish);
}
