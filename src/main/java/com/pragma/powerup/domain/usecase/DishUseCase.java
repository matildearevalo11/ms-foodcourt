package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;

public class DishUseCase implements IDishServicePort {
    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    public DishUseCase(IDishPersistencePort dishPersistencePort,
                       IRestaurantPersistencePort restaurantPersistencePort) {
        this.dishPersistencePort = dishPersistencePort;
        this.restaurantPersistencePort = restaurantPersistencePort;
    }

    @Override
    public Dish saveDish(Dish dish) {
        validatePrice(dish.getPrice());
        if (!restaurantPersistencePort.existsById(dish.getRestaurantId())) {
            throw new ValidationException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage());
        }
        if (!dishPersistencePort.categoryExistsById(dish.getCategoryId())) {
            throw new ValidationException(ExceptionMessages.CATEGORY_NOT_FOUND.getMessage());
        }
        dish.setActive(true);
        return dishPersistencePort.saveDish(dish);
    }

    @Override
    public Dish updateDish(Long restaurantId, Long dishId, Long price, String description) {
        validatePrice(price);
        Dish dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.DISH_NOT_FOUND.getMessage()));
        if (!restaurantId.equals(dish.getRestaurantId())) {
            throw new NotFoundException(ExceptionMessages.DISH_NOT_FOUND.getMessage());
        }
        dish.setPrice(price);
        dish.setDescription(description);
        return dishPersistencePort.saveDish(dish);
    }

    private void validatePrice(Long price) {
        if (price == null || price <= 0) {
            throw new ValidationException(ExceptionMessages.INVALID_DISH_PRICE.getMessage());
        }
    }
}
