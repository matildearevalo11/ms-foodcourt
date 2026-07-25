package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IRestaurantEntityMapper {
    RestaurantEntity toEntity(Restaurant restaurant);
    Restaurant toRestaurant(RestaurantEntity entity);
}
