package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.DishResponseDto;
import com.pragma.powerup.domain.model.Dish;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface IDishResponseMapper {
    DishResponseDto toResponse(Dish dish);
    List<DishResponseDto> toResponseList(List<Dish> dishes);
}
