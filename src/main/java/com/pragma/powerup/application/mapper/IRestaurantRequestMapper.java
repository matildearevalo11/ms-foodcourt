package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.domain.model.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IRestaurantRequestMapper {
    @Mapping(target = "id", ignore = true)
    Restaurant toRestaurant(RestaurantRequestDto request);
}
