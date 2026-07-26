package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishRequestDto {
    @NotBlank(message = "Dish name is required")
    private String name;

    @NotNull(message = "Dish price is required")
    @Positive(message = "Dish price must be greater than zero")
    private Long price;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Image URL is required")
    private String urlImage;

    @NotNull(message = "Category id is required")
    @Positive(message = "Category id must be positive")
    private Long categoryId;
}
