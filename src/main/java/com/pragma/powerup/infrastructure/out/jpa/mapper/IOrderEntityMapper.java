package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IOrderEntityMapper {

    @Mapping(target = "restaurant.id", source = "restaurantId")
    @Mapping(target = "securityPin", ignore = true)
    OrderEntity toEntity(Order order);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "items", ignore = true)
    Order toOrder(OrderEntity entity);
}
