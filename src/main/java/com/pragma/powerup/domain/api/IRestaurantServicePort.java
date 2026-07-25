package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.Restaurant;

public interface IRestaurantServicePort {
    Restaurant saveRestaurant(Restaurant restaurant);
}
