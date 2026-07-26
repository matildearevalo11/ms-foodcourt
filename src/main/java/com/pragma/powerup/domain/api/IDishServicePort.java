package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Dish;

public interface IDishServicePort {
    Dish saveDish(Dish dish);
    Dish updateDish(Long restaurantId, Long dishId, Long price, String description);
}
