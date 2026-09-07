package com.pragma.powerup.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantUseCaseTest {
    @Mock
    IRestaurantPersistencePort persistencePort;

    @Mock
    IOwnerValidationPort ownerValidationPort;
    private RestaurantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RestaurantUseCase(persistencePort, ownerValidationPort);
    }

    @Test
    void createsRestaurantForAnOwnerAndNormalizesItsFields() {
        Restaurant restaurant = validRestaurant();
        when(ownerValidationPort.isOwner(7L)).thenReturn(true);
        when(persistencePort.save(restaurant)).thenReturn(restaurant);

        Restaurant result = useCase.createRestaurant(restaurant);

        assertThat(result.getName()).isEqualTo("Restaurante 123");
        assertThat(result.getPhone()).isEqualTo("+573005698325");
        verify(persistencePort).save(restaurant);
    }

    @Test
    void rejectsDuplicatedNitBeforeCallingUsers() {
        Restaurant restaurant = validRestaurant();
        when(persistencePort.existsByNit("900123456")).thenReturn(true);

        assertThatThrownBy(() -> useCase.createRestaurant(restaurant))
                .isInstanceOf(ValidationException.class);
        verify(ownerValidationPort, never()).isOwner(7L);
    }

    @Test
    void rejectsAUserWithoutOwnerRole() {
        Restaurant restaurant = validRestaurant();
        when(ownerValidationPort.isOwner(7L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.createRestaurant(restaurant))
                .isInstanceOf(ValidationException.class);
        verify(persistencePort, never()).save(restaurant);
    }

    private Restaurant validRestaurant() {
        return new Restaurant(null, " Restaurante 123 ", " 900123456 ", " Local 15 ",
                " +573005698325 ", " https://cdn.example.com/logo.png ", 7L);
    }
}
