package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.RestaurantSummaryResponseDto;
import com.pragma.powerup.domain.model.Restaurant;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IRestaurantSummaryResponseMapper {
    List<RestaurantSummaryResponseDto> toResponseList(List<Restaurant> restaurants);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "urlLogo", source = "urlLogo")
    RestaurantSummaryResponseDto toResponse(Restaurant restaurant);
}
