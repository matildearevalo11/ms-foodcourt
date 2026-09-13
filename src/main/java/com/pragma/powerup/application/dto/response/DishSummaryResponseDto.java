package com.pragma.powerup.application.dto.response;

public record DishSummaryResponseDto(Long id, String name, Long price, String description, String urlImage,
        Long categoryId) { }
