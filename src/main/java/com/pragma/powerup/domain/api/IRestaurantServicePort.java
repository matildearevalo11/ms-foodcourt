package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.model.PageResult;

public interface IRestaurantServicePort {
    Restaurant saveRestaurant(Restaurant restaurant);
    PageResult<Restaurant> getRestaurants(int page, int size);
}
