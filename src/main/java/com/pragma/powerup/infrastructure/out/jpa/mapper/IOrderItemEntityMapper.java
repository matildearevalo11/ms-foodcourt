package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderItemEntityMapper {

    @Mapping(target = "dish.id", source = "dishId")
    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItem orderItem);

    @Mapping(target = "dishId", source = "dish.id")
    OrderItem toOrderItem(OrderItemEntity entity);

    List<OrderItemEntity> toEntities(List<OrderItem> orderItems);
    List<OrderItem> toOrderItems(List<OrderItemEntity> entities);
}
