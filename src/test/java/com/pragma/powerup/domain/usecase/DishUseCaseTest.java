package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishUseCaseTest {
    private IDishPersistencePort dishPersistencePort;
    private IRestaurantPersistencePort restaurantPersistencePort;
    private DishUseCase useCase;

    @BeforeEach
    void setUp() {
        dishPersistencePort = mock(IDishPersistencePort.class);
        restaurantPersistencePort = mock(IRestaurantPersistencePort.class);
        useCase = new DishUseCase(dishPersistencePort, restaurantPersistencePort);
    }

    @Test
    void saveDish_WithValidData_ShouldEnableAndPersistDish() {
        Dish dish = validDish();
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
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
        assertThrows(ValidationException.class, () -> useCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    @Test
    void saveDish_WhenCategoryDoesNotExist_ShouldFail() {
        Dish dish = validDish();
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
        assertThrows(ValidationException.class, () -> useCase.saveDish(dish));
        verify(dishPersistencePort, never()).saveDish(any());
    }

    private Dish validDish() {
        return new Dish(null, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, false);
    }
}
