package com.pragma.powerup.application.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DishResponseDto {
    private Long id;
    private String name;
    private Long price;
    private String description;
    private String urlImage;
    private Long categoryId;
    private Long restaurantId;
    private boolean active;
}
