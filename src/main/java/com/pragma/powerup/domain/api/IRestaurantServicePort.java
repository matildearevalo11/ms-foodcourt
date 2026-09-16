package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;

public interface IRestaurantServicePort {
    Restaurant createRestaurant(Restaurant restaurant);

    PageResult<Restaurant> getRestaurants(int page, int size);

    void validateOwnership(Long restaurantId);
}
