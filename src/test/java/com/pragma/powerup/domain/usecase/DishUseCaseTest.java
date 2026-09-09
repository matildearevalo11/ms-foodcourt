package com.pragma.powerup.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

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

    @Test
    void updatesOnlyPriceAndDescription() {
        Dish dish = validDish();
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        when(dishPersistencePort.save(dish)).thenReturn(dish);

        Dish result = useCase.updateDish(5L, 10L, 30000L, "Nueva descripción");

        assertThat(result.getPrice()).isEqualTo(30000L);
        assertThat(result.getDescription()).isEqualTo("Nueva descripción");
        assertThat(result.getName()).isEqualTo("Hamburguesa");
        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getRestaurantId()).isEqualTo(5L);
    }

    @Test
    void rejectsMissingDishOrDishFromAnotherRestaurant() {
        when(dishPersistencePort.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.updateDish(5L, 99L, 30000L, "Descripción"))
                .isInstanceOf(NotFoundException.class);

        Dish dish = validDish();
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        assertThatThrownBy(() -> useCase.updateDish(8L, 10L, 30000L, "Descripción"))
                .isInstanceOf(NotFoundException.class);
        verify(dishPersistencePort, never()).save(dish);
    }

    private Dish validDish() {
        return new Dish(null, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, false);
    }
}
