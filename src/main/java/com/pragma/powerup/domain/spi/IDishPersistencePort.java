package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Dish;

public interface IDishPersistencePort {
    Dish saveDish(Dish dish);
    boolean categoryExistsById(Long categoryId);
}
