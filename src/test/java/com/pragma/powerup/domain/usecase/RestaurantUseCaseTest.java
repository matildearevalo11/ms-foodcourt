package com.pragma.powerup.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
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

    @Mock
    ILoggedUserPort loggedUserPort;

    private RestaurantUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RestaurantUseCase(persistencePort, ownerValidationPort, loggedUserPort);
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

    @Test
    void validatesRestaurantOwnership() {
        Restaurant restaurant = validRestaurant();
        when(persistencePort.findById(1L)).thenReturn(java.util.Optional.of(restaurant));
        when(loggedUserPort.getUserId()).thenReturn(7L);

        useCase.validateOwnership(1L);

        verify(loggedUserPort).getUserId();
    }

    @Test
    void rejectsRestaurantOwnedByAnotherUser() {
        Restaurant restaurant = validRestaurant();
        when(persistencePort.findById(1L)).thenReturn(java.util.Optional.of(restaurant));
        when(loggedUserPort.getUserId()).thenReturn(8L);

        assertThatThrownBy(() -> useCase.validateOwnership(1L))
                .isInstanceOf(AuthorizationException.class);
    }

    private Restaurant validRestaurant() {
        return new Restaurant(null, " Restaurante 123 ", " 900123456 ", " Local 15 ",
                " +573005698325 ", " https://cdn.example.com/logo.png ", 7L);
    }
}
