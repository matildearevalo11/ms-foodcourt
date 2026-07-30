package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.OrderResponseDto;
import com.pragma.powerup.domain.model.Order;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface IOrderResponseMapper {

    OrderResponseDto toResponse(Order order);
    List<OrderResponseDto> toResponseList(List<Order> orders);
}
