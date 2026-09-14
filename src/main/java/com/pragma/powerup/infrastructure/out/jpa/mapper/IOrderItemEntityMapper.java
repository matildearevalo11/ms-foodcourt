package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderItemEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IOrderItemEntityMapper {
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "dish.id", source = "dishId")
    OrderItemEntity toEntity(OrderItem item);

    List<OrderItemEntity> toEntityList(List<OrderItem> items);

    @Mapping(target = "dishId", source = "dish.id")
    OrderItem toDomain(OrderItemEntity entity);

    List<OrderItem> toDomainList(List<OrderItemEntity> entities);
}
