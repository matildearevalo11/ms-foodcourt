package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import java.util.Optional;

public interface IDishPersistencePort {
    Dish save(Dish dish);

    Optional<Dish> findById(Long dishId);

    PageResult<Dish> findActiveByRestaurant(Long restaurantId, Long categoryId, int page, int size);
}
