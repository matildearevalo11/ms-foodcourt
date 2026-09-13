package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.RestaurantRequestDto;
import com.pragma.powerup.application.dto.response.PageMetadataDto;
import com.pragma.powerup.application.dto.response.PageResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantResponseDto;
import com.pragma.powerup.application.dto.response.RestaurantSummaryResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.application.mapper.IRestaurantRequestMapper;
import com.pragma.powerup.application.mapper.IRestaurantResponseMapper;
import com.pragma.powerup.application.mapper.IRestaurantSummaryResponseMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantHandler implements IRestaurantHandler {
    private final IRestaurantServicePort servicePort;
    private final IRestaurantRequestMapper requestMapper;
    private final IRestaurantResponseMapper responseMapper;
    private final IRestaurantSummaryResponseMapper summaryResponseMapper;

    @Override
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto request) {
        return responseMapper.toResponse(servicePort.createRestaurant(requestMapper.toRestaurant(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<RestaurantSummaryResponseDto> getRestaurants(int page, int size) {
        PageResult<Restaurant> result = servicePort.getRestaurants(page, size);
        PageMetadataDto metadata = new PageMetadataDto(
                result.page(), result.size(), result.totalElements(), result.totalPages());
        return new PageResponseDto<>(summaryResponseMapper.toResponseList(result.content()), metadata);
    }

    @Override
    public void validateOwnership(Long restaurantId) {
        servicePort.validateOwnership(restaurantId);
    }
}
