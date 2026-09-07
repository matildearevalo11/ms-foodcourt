package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestaurantUseCase implements IRestaurantServicePort {
    private final IRestaurantPersistencePort persistencePort;
    private final IOwnerValidationPort ownerValidationPort;

    @Override
    public Restaurant createRestaurant(Restaurant restaurant) {
        normalize(restaurant);
        validateNitAvailability(restaurant.getNit());
        validateOwner(restaurant.getOwnerId());
        return persistencePort.save(restaurant);
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
