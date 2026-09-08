package com.pragma.powerup.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DishUseCaseTest {
    @Mock
    IDishPersistencePort dishPersistencePort;

    @Mock
    IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    ICategoryPersistencePort categoryPersistencePort;

    private DishUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DishUseCase(dishPersistencePort, restaurantPersistencePort, categoryPersistencePort);
    }

    @Test
    void createsAnActiveDishForExistingRestaurantAndCategory() {
        Dish dish = validDish();
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
        when(categoryPersistencePort.existsById(2L)).thenReturn(true);
        when(dishPersistencePort.save(dish)).thenReturn(dish);

        Dish result = useCase.createDish(dish);

        assertThat(result.isActive()).isTrue();
        verify(dishPersistencePort).save(dish);
    }

    @Test
    void rejectsUnknownRestaurant() {
        Dish dish = validDish();

        assertThatThrownBy(() -> useCase.createDish(dish))
                .isInstanceOf(ValidationException.class);
        verify(categoryPersistencePort, never()).existsById(2L);
    }

    @Test
    void rejectsUnknownCategory() {
        Dish dish = validDish();
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.createDish(dish))
                .isInstanceOf(ValidationException.class);
        verify(dishPersistencePort, never()).save(dish);
    }

    private Dish validDish() {
        return new Dish(null, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, false);
    }
}
