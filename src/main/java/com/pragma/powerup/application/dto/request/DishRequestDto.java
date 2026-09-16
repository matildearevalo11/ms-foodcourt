package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DishRequestDto(
        @NotBlank(message = "Dish name is required")
        String name,

        @NotNull(message = "Dish price is required")
        @Positive(message = "Dish price must be greater than zero")
        Long price,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Image URL is required")
        String urlImage,

        @NotNull(message = "Category id is required")
        @Positive(message = "Category id must be positive")
        Long categoryId
) { }
