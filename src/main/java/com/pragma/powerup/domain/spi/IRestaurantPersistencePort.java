package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.model.PageResult;
import java.util.Optional;

public interface IRestaurantPersistencePort {
    Restaurant saveRestaurant(Restaurant restaurant);
    boolean existsByNit(String nit);
    boolean existsById(Long restaurantId);
    Optional<Restaurant> findById(Long restaurantId);
    PageResult<Restaurant> findAllByNameAsc(int page, int size);
}
