package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;
import java.util.Optional;

public interface IRestaurantPersistencePort {
    Restaurant save(Restaurant restaurant);
    boolean existsByNit(String nit);
    Optional<Restaurant> findById(Long restaurantId);
    PageResult<Restaurant> findAllByNameAsc(int page, int size);
}
