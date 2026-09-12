package com.pragma.powerup.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import java.util.Optional;
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

    @Mock
    ILoggedUserPort loggedUserPort;

    private DishUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DishUseCase(dishPersistencePort, restaurantPersistencePort, categoryPersistencePort, loggedUserPort);
    }

    @Test
    void createsAnActiveDishForExistingRestaurantAndCategory() {
        Dish dish = validDish();
        ownerRestaurant(5L);
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
                .isInstanceOf(NotFoundException.class);
        verify(categoryPersistencePort, never()).existsById(2L);
    }

    @Test
    void rejectsUnknownCategory() {
        Dish dish = validDish();
        ownerRestaurant(5L);

        assertThatThrownBy(() -> useCase.createDish(dish))
                .isInstanceOf(ValidationException.class);
        verify(dishPersistencePort, never()).save(dish);
    }

    @Test
    void updatesOnlyPriceAndDescription() {
        Dish dish = validDish();
        ownerRestaurant(5L);
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
        ownerRestaurant(5L);
        when(dishPersistencePort.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.updateDish(5L, 99L, 30000L, "Descripción"))
                .isInstanceOf(NotFoundException.class);

        Dish dish = validDish();
        ownerRestaurant(8L);
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        assertThatThrownBy(() -> useCase.updateDish(8L, 10L, 30000L, "Descripción"))
                .isInstanceOf(NotFoundException.class);
        verify(dishPersistencePort, never()).save(dish);
    }

    @Test
    void rejectsOwnerFromAnotherRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(9L);
        when(restaurantPersistencePort.findById(5L)).thenReturn(Optional.of(restaurant));
        when(loggedUserPort.getUserId()).thenReturn(7L);

        assertThatThrownBy(() -> useCase.createDish(validDish()))
                .isInstanceOf(AuthorizationException.class);
        verify(dishPersistencePort, never()).save(validDish());
    }

    @Test
    void updatesDishStatusForItsRestaurantOwner() {
        Dish dish = validDish();
        dish.setActive(true);
        ownerRestaurant(5L);
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));
        when(dishPersistencePort.save(dish)).thenReturn(dish);

        Dish result = useCase.updateDishStatus(5L, 10L, false);

        assertThat(result.isActive()).isFalse();
        verify(dishPersistencePort).save(dish);
    }

    @Test
    void rejectsStatusChangeForDishFromAnotherRestaurant() {
        Dish dish = validDish();
        ownerRestaurant(8L);
        when(dishPersistencePort.findById(10L)).thenReturn(Optional.of(dish));

        assertThatThrownBy(() -> useCase.updateDishStatus(8L, 10L, false))
                .isInstanceOf(NotFoundException.class);
        verify(dishPersistencePort, never()).save(dish);
    }

    private void ownerRestaurant(Long restaurantId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(7L);
        when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(loggedUserPort.getUserId()).thenReturn(7L);
    }

    private Dish validDish() {
        return new Dish(null, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, 5L, false);
    }
}
