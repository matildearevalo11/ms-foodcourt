package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.DishSummaryResponseDto;
import com.pragma.powerup.domain.model.Dish;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IDishSummaryResponseMapper {

    List<DishSummaryResponseDto> toResponseList(List<Dish> dishes);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "urlImage", source = "urlImage")
    @Mapping(target = "categoryId", source = "categoryId")
    DishSummaryResponseDto toResponse(Dish dish);
}
