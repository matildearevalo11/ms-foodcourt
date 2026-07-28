package com.pragma.powerup.application.mapper;

import com.pragma.powerup.application.dto.response.RestaurantSummaryResponseDto;
import com.pragma.powerup.domain.model.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IRestaurantSummaryResponseMapper {
    List<RestaurantSummaryResponseDto> toResponseList(List<Restaurant> restaurants);
}
