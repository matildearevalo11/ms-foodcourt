package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import java.util.Optional;
import java.util.List;

public interface IDishPersistencePort {
    Dish saveDish(Dish dish);
    boolean categoryExistsById(Long categoryId);
    Optional<Dish> findById(Long dishId);
    PageResult<Dish> findActiveDishes(Long restaurantId, Long categoryId, int page, int size);
    List<Dish> findAllByIds(Iterable<Long> dishIds);
}
