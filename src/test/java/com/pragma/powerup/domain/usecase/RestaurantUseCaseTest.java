package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RestaurantUseCaseTest {
    @Mock private IRestaurantPersistencePort persistencePort;
    @Mock private IOwnerValidationPort ownerValidationPort;
    private RestaurantUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        useCase = new RestaurantUseCase(persistencePort, ownerValidationPort);
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

    private Restaurant validRestaurant() {
        return new Restaurant(null, "El Corral 2", "900123456", "Local 15",
                "+573005698325", "https://cdn.example.com/logo.png", 7L);
    }
}
