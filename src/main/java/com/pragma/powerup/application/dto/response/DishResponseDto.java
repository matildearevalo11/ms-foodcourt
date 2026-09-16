package com.pragma.powerup.application.dto.response;

public record DishResponseDto(Long id, String name, Long price, String description, String urlImage, Long categoryId,
        Long restaurantId, boolean active){ }
