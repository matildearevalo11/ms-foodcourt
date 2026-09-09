package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Dish;
import java.util.Optional;

public interface IDishPersistencePort {
    Dish save(Dish dish);

    Optional<Dish> findById(Long dishId);
}
