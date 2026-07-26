package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dish {
    private Long id;
    private String name;
    private Long price;
    private String description;
    private String urlImage;
    private Long categoryId;
    private Long restaurantId;
    private boolean active;
}
