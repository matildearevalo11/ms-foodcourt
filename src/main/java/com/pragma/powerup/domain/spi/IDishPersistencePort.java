package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IDishPersistencePort {
    Dish save(Dish dish);

    Optional<Dish> findById(Long dishId);

    PageResult<Dish> findActiveByRestaurant(Long restaurantId, Long categoryId, int page, int size);

    List<Dish> findAllById(Set<Long> dishIds);
}
