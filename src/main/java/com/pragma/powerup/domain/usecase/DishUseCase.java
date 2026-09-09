package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DishUseCase implements IDishServicePort {
    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ICategoryPersistencePort categoryPersistencePort;

    @Override
    public Dish createDish(Dish dish) {
        validateRestaurant(dish.getRestaurantId());
        validateCategory(dish.getCategoryId());
        dish.setActive(true);
        return dishPersistencePort.save(dish);
    }

    @Override
    public Dish updateDish(Long restaurantId, Long dishId, Long price, String description) {
        Dish dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.DISH_NOT_FOUND.getMessage()));

        if (!restaurantId.equals(dish.getRestaurantId())) {
            throw new NotFoundException(ExceptionMessages.DISH_NOT_FOUND.getMessage());
        }
        dish.setPrice(price);
        dish.setDescription(description);
        return dishPersistencePort.save(dish);
    }

    private void validateRestaurant(Long restaurantId) {
        if (!restaurantPersistencePort.existsById(restaurantId)) {
            throw new ValidationException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage());
        }
    }

    private void validateCategory(Long categoryId) {
        if (!categoryPersistencePort.existsById(categoryId)) {
            throw new ValidationException(ExceptionMessages.CATEGORY_NOT_FOUND.getMessage());
        }
    }
}
