package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestaurantUseCase implements IRestaurantServicePort {
    private final IRestaurantPersistencePort persistencePort;
    private final IOwnerValidationPort ownerValidationPort;
    private final ILoggedUserPort loggedUserPort;

    @Override
    public Restaurant createRestaurant(Restaurant restaurant) {
        normalize(restaurant);
        validateNitAvailability(restaurant.getNit());
        validateOwner(restaurant.getOwnerId());
        return persistencePort.save(restaurant);
    }

    @Override
    public PageResult<Restaurant> getRestaurants(int page, int size) {
        return persistencePort.findAllByNameAsc(page, size);
    }

    @Override
    public void validateOwnership(Long restaurantId) {
        Restaurant restaurant = persistencePort.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException(ExceptionMessages.RESTAURANT_NOT_FOUND.getMessage()));
        if (!restaurant.getOwnerId().equals(loggedUserPort.getUserId())) {
            throw new AuthorizationException(ExceptionMessages.RESTAURANT_OWNER_REQUIRED.getMessage());
        }
    }

    private void normalize(Restaurant restaurant) {
        restaurant.setName(restaurant.getName().trim());
        restaurant.setNit(restaurant.getNit().trim());
        restaurant.setAddress(restaurant.getAddress().trim());
        restaurant.setPhone(restaurant.getPhone().trim());
        restaurant.setUrlLogo(restaurant.getUrlLogo().trim());
    }

    private void validateNitAvailability(String nit) {
        if (persistencePort.existsByNit(nit)) {
            throw new ValidationException(ExceptionMessages.NIT_ALREADY_EXISTS.getMessage());
        }
    }

    private void validateOwner(Long ownerId) {
        if (!ownerValidationPort.isOwner(ownerId)) {
            throw new ValidationException(ExceptionMessages.OWNER_ROLE_REQUIRED.getMessage());
        }
    }
}
