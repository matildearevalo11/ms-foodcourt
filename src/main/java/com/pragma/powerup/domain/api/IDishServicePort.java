package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;

public interface IDishServicePort {
    Dish saveDish(Dish dish);
    Dish updateDish(Long restaurantId, Long dishId, Long price, String description);
    Dish updateDishStatus(Long restaurantId, Long dishId, boolean active);
    PageResult<Dish> getDishes(Long restaurantId, Long categoryId, int page, int size);
}
