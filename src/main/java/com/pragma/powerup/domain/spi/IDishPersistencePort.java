package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Dish;
import java.util.Optional;

public interface IDishPersistencePort {
    Dish saveDish(Dish dish);
    boolean categoryExistsById(Long categoryId);
    Optional<Dish> findById(Long dishId);
}
