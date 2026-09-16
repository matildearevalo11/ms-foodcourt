package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.domain.model.Restaurant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IRestaurantResponseMapper {
    RestaurantResponseDto toResponse(Restaurant restaurant);
}
