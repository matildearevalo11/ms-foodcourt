package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class RestaurantUseCase implements IRestaurantServicePort {
    private static final Pattern NUMERIC = Pattern.compile("\\d+");
    private static final Pattern PHONE = Pattern.compile("\\+?\\d{1,13}");
    private final IRestaurantPersistencePort persistencePort;
    private final IOwnerValidationPort ownerValidationPort;
    private final ILoggedUserPort loggedUserPort;

    @Override
    public Restaurant saveRestaurant(Restaurant restaurant) {
        validateBusinessRules(restaurant);
        if (persistencePort.existsByNit(restaurant.getNit())) {
            throw new ValidationException(ExceptionMessages.NIT_ALREADY_EXISTS.getMessage());
        }
        if (!ownerValidationPort.isOwner(restaurant.getOwnerId())) {
            throw new ValidationException(ExceptionMessages.OWNER_ROLE_REQUIRED.getMessage());
        }
        return persistencePort.saveRestaurant(restaurant);
    }

    @Override
    public PageResult<Restaurant> getRestaurants(int page, int size) {
        return persistencePort.findAllByNameAsc(page, size);
    }

    @Override
    public void validateLoggedOwner(Long restaurantId) {
        Restaurant restaurant = persistencePort.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage()));
        if (!loggedUserPort.getLoggedUserId().equals(restaurant.getOwnerId())) {
            throw new AuthorizationException(ExceptionMessages.RESTAURANT_OWNER_REQUIRED.getMessage());
        }
    }

    private void validateBusinessRules(Restaurant restaurant) {
        if (NUMERIC.matcher(restaurant.getName()).matches()) {
            throw new ValidationException(ExceptionMessages.RESTAURANT_NAME_NUMERIC.getMessage());
        }
        if (!NUMERIC.matcher(restaurant.getNit()).matches()) {
            throw new ValidationException(ExceptionMessages.INVALID_NIT.getMessage());
        }
        if (restaurant.getPhone().length() > 13 || !PHONE.matcher(restaurant.getPhone()).matches()) {
            throw new ValidationException(ExceptionMessages.INVALID_PHONE.getMessage());
        }
    }
}
