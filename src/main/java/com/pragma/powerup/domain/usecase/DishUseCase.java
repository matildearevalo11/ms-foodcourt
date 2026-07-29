package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DishUseCase implements IDishServicePort {
    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ILoggedUserPort loggedUserPort;

    @Override
    public Dish saveDish(Dish dish) {
        validatePrice(dish.getPrice());
        validateRestaurantOwner(dish.getRestaurantId());
        if (!dishPersistencePort.categoryExistsById(dish.getCategoryId())) {
            throw new ValidationException(ExceptionMessages.CATEGORY_NOT_FOUND.getMessage());
        }
        dish.setActive(true);
        return dishPersistencePort.saveDish(dish);
    }

    @Override
    public Dish updateDish(Long restaurantId, Long dishId, Long price, String description) {
        validatePrice(price);
        Dish dish = findOwnedDish(restaurantId, dishId);
        dish.setPrice(price);
        dish.setDescription(description);
        return dishPersistencePort.saveDish(dish);
    }

    @Override
    public Dish updateDishStatus(Long restaurantId, Long dishId, boolean active) {
        Dish dish = findOwnedDish(restaurantId, dishId);
        dish.setActive(active);
        return dishPersistencePort.saveDish(dish);
    }

    @Override
    public PageResult<Dish> getDishes(Long restaurantId, Long categoryId, int page, int size) {
        if (!restaurantPersistencePort.existsById(restaurantId)) {
            throw new NotFoundException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage());
        }
        return dishPersistencePort.findActiveDishes(restaurantId, categoryId, page, size);
    }

    private void validatePrice(Long price) {
        if (price == null || price <= 0) {
            throw new ValidationException(ExceptionMessages.INVALID_DISH_PRICE.getMessage());
        }
    }

    private void validateRestaurantOwner(Long restaurantId) {
        Restaurant restaurant = restaurantPersistencePort.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage()));
        if (!loggedUserPort.getLoggedUserId().equals(restaurant.getOwnerId())) {
            throw new AuthorizationException(ExceptionMessages.RESTAURANT_OWNER_REQUIRED.getMessage());
        }
    }

    private Dish findOwnedDish(Long restaurantId, Long dishId) {
        validateRestaurantOwner(restaurantId);
        Dish dish = dishPersistencePort.findById(dishId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.DISH_NOT_FOUND.getMessage()));
        if (!restaurantId.equals(dish.getRestaurantId())) {
            throw new NotFoundException(ExceptionMessages.DISH_NOT_FOUND.getMessage());
        }
        return dish;
    }
}
