package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

class DishUseCaseTest {
    private IDishPersistencePort dishPersistencePort;
    private IRestaurantPersistencePort restaurantPersistencePort;
    private ILoggedUserPort loggedUserPort;
    private DishUseCase useCase;

    @BeforeEach
    void setUp() {
        dishPersistencePort = mock(IDishPersistencePort.class);
        restaurantPersistencePort = mock(IRestaurantPersistencePort.class);
        loggedUserPort = mock(ILoggedUserPort.class);
        when(loggedUserPort.getLoggedUserId()).thenReturn(7L);
        useCase = new DishUseCase(dishPersistencePort, restaurantPersistencePort, loggedUserPort);
    }

    @Test
    void saveDish_WithValidData_ShouldEnableAndPersistDish() {
        Dish dish = validDish();
        ownerRestaurantExists();
        when(dishPersistencePort.categoryExistsById(2L)).thenReturn(true);
        when(dishPersistencePort.saveDish(dish)).thenReturn(dish);

        Dish result = useCase.saveDish(dish);

        assertTrue(result.isActive());
        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void saveDish_WithNullPrice_ShouldFail() {
        Dish dish = validDish();
        dish.setPrice(null);
        assertThrows(ValidationException.class, () -> useCase.saveDish(dish));
        verifyNoInteractions(restaurantPersistencePort, dishPersistencePort);
    }

    @Test
    void saveDish_WithNonPositivePrice_ShouldFail() {
        Dish dish = validDish();
        dish.setPrice(0L);
        assertThrows(ValidationException.class, () -> useCase.saveDish(dish));
    }

    @Test
    void saveDish_WhenRestaurantDoesNotExist_ShouldFail() {
        Dish dish = validDish();
        assertThrows(NotFoundException.class, () -> useCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_WhenCategoryDoesNotExist_ShouldFail() {
        Dish dish = validDish();
        ownerRestaurantExists();
        assertThrows(ValidationException.class, () -> useCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDish_WithValidData_ShouldOnlyUpdatePriceAndDescription() {
        Dish dish = validDish();
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        when(dishPersistencePort.saveDish(dish)).thenReturn(dish);

        ownerRestaurantExists();
        Dish result = useCase.updateDish(5L, 10L, 30000L, "Nueva descripción");

        assertEquals(30000L, result.getPrice());
        assertEquals("Nueva descripción", result.getDescription());
        assertEquals("Hamburguesa", result.getName());
        assertEquals(2L, result.getCategoryId());
        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void updateDish_WithInvalidPrice_ShouldFailBeforeLookup() {
        assertThrows(ValidationException.class,
                () -> useCase.updateDish(5L, 10L, 0L, "Descripción"));
        verifyNoInteractions(dishPersistencePort);
    }

    @Test
    void updateDish_WhenDishDoesNotExist_ShouldThrowNotFoundException() {
        when(dishPersistencePort.findById(99L)).thenReturn(Optional.empty());
        ownerRestaurantExists();
        assertThrows(NotFoundException.class,
                () -> useCase.updateDish(5L, 99L, 30000L, "Descripción"));
    }

    @Test
    void updateDish_WhenDishBelongsToAnotherRestaurant_ShouldThrowNotFoundException() {
        Dish dish = validDish();
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        assertThrows(NotFoundException.class,
                () -> useCase.updateDish(8L, 10L, 30000L, "Descripción"));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_WhenAuthenticatedOwnerDoesNotOwnRestaurant_ShouldFail() {
        ownerRestaurantExists();
        when(loggedUserPort.getLoggedUserId()).thenReturn(9L);
        assertThrows(AuthorizationException.class, () -> useCase.saveDish(validDish()));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void updateDishStatus_WhenOwnerOwnsRestaurant_ShouldPersistRequestedStatus() {
        Dish dish = validDish();
        dish.setActive(true);
        ownerRestaurantExists();
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        when(dishPersistencePort.saveDish(dish)).thenReturn(dish);

        Dish result = useCase.updateDishStatus(5L, 10L, false);

        assertFalse(result.isActive());
        verify(dishPersistencePort).saveDish(dish);
    }

    @Test
    void updateDishStatus_WhenDishBelongsToAnotherRestaurant_ShouldFail() {
        Dish dish = validDish();
        dish.setRestaurantId(8L);
        ownerRestaurantExists();
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));

        assertThrows(NotFoundException.class, () -> useCase.updateDishStatus(5L, 10L, false));
    }

    @Test
    void updateDishStatus_WhenLoggedOwnerDoesNotOwnRestaurant_ShouldFail() {
        ownerRestaurantExists();
        when(loggedUserPort.getLoggedUserId()).thenReturn(9L);

        assertThrows(AuthorizationException.class, () -> useCase.updateDishStatus(5L, 10L, false));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    private void ownerRestaurantExists() {
        Restaurant restaurant = new Restaurant(5L, "Restaurante", "123", "Dirección",
                "3001234567", "logo", 7L);
        when(restaurantPersistencePort.findById(5L)).thenReturn(Optional.of(restaurant));
    }

    private Dish validDish() {
        return new Dish(null, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, false);
    }
}
