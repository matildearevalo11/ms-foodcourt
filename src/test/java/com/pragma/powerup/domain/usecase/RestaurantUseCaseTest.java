package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import java.util.List;

class RestaurantUseCaseTest {
    @Mock private IRestaurantPersistencePort persistencePort;
    @Mock private IOwnerValidationPort ownerValidationPort;
    @Mock private ILoggedUserPort loggedUserPort;
    private RestaurantUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new RestaurantUseCase(persistencePort, ownerValidationPort, loggedUserPort);
    }

    @Test
    void saveRestaurant_WithValidDataAndOwner_ShouldPersist() {
        Restaurant restaurant = validRestaurant();
        Restaurant persisted = validRestaurant();
        persisted.setId(1L);
        when(ownerValidationPort.isOwner(7L)).thenReturn(true);
        when(persistencePort.saveRestaurant(restaurant)).thenReturn(persisted);

        Restaurant result = useCase.saveRestaurant(restaurant);

        assertEquals(1L, result.getId());
        verify(persistencePort).saveRestaurant(restaurant);
    }

    @Test
    void saveRestaurant_WhenNitAlreadyExists_ShouldFailBeforeRemoteCall() {
        Restaurant restaurant = validRestaurant();
        when(persistencePort.existsByNit(restaurant.getNit())).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.saveRestaurant(restaurant));

        verifyNoInteractions(ownerValidationPort);
        verify(persistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurant_WhenUserIsNotOwner_ShouldFail() {
        Restaurant restaurant = validRestaurant();
        when(ownerValidationPort.isOwner(restaurant.getOwnerId())).thenReturn(false);

        assertThrows(ValidationException.class, () -> useCase.saveRestaurant(restaurant));
        verify(persistencePort, never()).saveRestaurant(any());
    }

    @Test
    void saveRestaurant_WhenNameIsOnlyNumeric_ShouldFail() {
        Restaurant restaurant = validRestaurant();
        restaurant.setName("12345");
        assertThrows(ValidationException.class, () -> useCase.saveRestaurant(restaurant));
        verifyNoInteractions(persistencePort, ownerValidationPort);
    }

    @Test
    void saveRestaurant_WhenNitIsNotNumeric_ShouldFail() {
        Restaurant restaurant = validRestaurant();
        restaurant.setNit("900A");
        assertThrows(ValidationException.class, () -> useCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_WhenPhoneHasInvalidCharacters_ShouldFail() {
        Restaurant restaurant = validRestaurant();
        restaurant.setPhone("300-123");
        assertThrows(ValidationException.class, () -> useCase.saveRestaurant(restaurant));
    }

    @Test
    void saveRestaurant_WhenPhoneExceedsThirteenCharacters_ShouldFail() {
        Restaurant restaurant = validRestaurant();
        restaurant.setPhone("+5730012345678");
        assertThrows(ValidationException.class, () -> useCase.saveRestaurant(restaurant));
    }

    @Test
    void getRestaurants_ShouldDelegatePagination() {
        PageResult<Restaurant> expected = new PageResult<>(List.of(validRestaurant()), 0, 5, 1, 1);
        when(persistencePort.findAllByNameAsc(0, 5)).thenReturn(expected);

        assertEquals(expected, useCase.getRestaurants(0, 5));
    }

    @Test
    void validateLoggedOwner_WhenRestaurantBelongsToOwner_ShouldPass() {
        Restaurant restaurant = validRestaurant();
        when(persistencePort.findById(5L)).thenReturn(java.util.Optional.of(restaurant));
        when(loggedUserPort.getLoggedUserId()).thenReturn(7L);

        useCase.validateLoggedOwner(5L);
    }

    @Test
    void validateLoggedOwner_WhenRestaurantBelongsToAnotherOwner_ShouldFail() {
        when(persistencePort.findById(5L)).thenReturn(java.util.Optional.of(validRestaurant()));
        when(loggedUserPort.getLoggedUserId()).thenReturn(9L);

        assertThrows(AuthorizationException.class, () -> useCase.validateLoggedOwner(5L));
    }

    @Test
    void validateLoggedOwner_WhenRestaurantDoesNotExist_ShouldFail() {
        assertThrows(NotFoundException.class, () -> useCase.validateLoggedOwner(99L));
    }

    private Restaurant validRestaurant() {
        return new Restaurant(null, "El Corral 2", "900123456", "Local 15",
                "+573005698325", "https://cdn.example.com/logo.png", 7L);
    }
}
