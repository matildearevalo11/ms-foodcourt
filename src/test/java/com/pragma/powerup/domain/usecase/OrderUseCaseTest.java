package com.pragma.powerup.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderUseCaseTest {
    @Mock
    IOrderPersistencePort orderPersistencePort;
    @Mock
    IDishPersistencePort dishPersistencePort;
    @Mock
    IRestaurantPersistencePort restaurantPersistencePort;
    @Mock
    ILoggedUserPort loggedUserPort;
    @Mock
    ITraceabilityPort traceabilityPort;

    private OrderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OrderUseCase(orderPersistencePort, dishPersistencePort,
                restaurantPersistencePort, loggedUserPort, traceabilityPort);
    }

    @Test
    void createsPendingOrderAndRegistersTraceability() {
        Order order = order(List.of(new OrderItem(null, 10L, 2)));
        when(loggedUserPort.getUserId()).thenReturn(20L);
        when(restaurantPersistencePort.findById(5L)).thenReturn(Optional.of(new Restaurant()));
        when(dishPersistencePort.findAllById(Set.of(10L))).thenReturn(List.of(dish(10L, 5L, true)));
        when(orderPersistencePort.save(order)).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(30L);
            return saved;
        });

        Order result = useCase.createOrder(order);

        assertThat(result.getCustomerId()).isEqualTo(20L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(traceabilityPort).registerStatusChange(result, null, null);
    }

    @Test
    void rejectsCustomerWithActiveOrderBeforeOtherValidations() {
        when(loggedUserPort.getUserId()).thenReturn(20L);
        when(orderPersistencePort.existsByCustomerIdAndStatusIn(20L, OrderStatus.activeStatuses()))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.createOrder(order(List.of(new OrderItem(null, 10L, 1)))))
                .isInstanceOf(ValidationException.class);

        verify(restaurantPersistencePort, never()).findById(any());
        verify(orderPersistencePort, never()).save(any());
    }

    @Test
    void rejectsDishesFromAnotherRestaurantOrInactiveDishes() {
        Order order = order(List.of(new OrderItem(null, 10L, 1)));
        when(loggedUserPort.getUserId()).thenReturn(20L);
        when(restaurantPersistencePort.findById(5L)).thenReturn(Optional.of(new Restaurant()));
        when(dishPersistencePort.findAllById(Set.of(10L))).thenReturn(List.of(dish(10L, 8L, false)));

        assertThatThrownBy(() -> useCase.createOrder(order))
                .isInstanceOf(ValidationException.class);

        verify(orderPersistencePort, never()).save(any());
        verify(traceabilityPort, never()).registerStatusChange(any(), any(), any());
    }

    @Test
    void rejectsDuplicatedDish() {
        Order order = order(List.of(
                new OrderItem(null, 10L, 1),
                new OrderItem(null, 10L, 2)));
        when(loggedUserPort.getUserId()).thenReturn(20L);
        when(restaurantPersistencePort.findById(5L)).thenReturn(Optional.of(new Restaurant()));

        assertThatThrownBy(() -> useCase.createOrder(order))
                .isInstanceOf(ValidationException.class);

        verify(dishPersistencePort, never()).findAllById(any());
    }

    @Test
    void listsOnlyOrdersFromAuthenticatedEmployeeRestaurant() {
        PageResult<Order> expected = new PageResult<>(List.of(order(List.of())), 0, 5, 1, 1);
        when(loggedUserPort.getRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.findByRestaurantIdAndStatus(5L, OrderStatus.PENDING, 0, 5))
                .thenReturn(expected);

        PageResult<Order> result = useCase.getOrdersByStatus(OrderStatus.PENDING, 0, 5);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void assignsPendingOrderToAuthenticatedEmployeeAndRegistersTraceability() {
        Order assignedOrder = order(List.of(new OrderItem(null, 10L, 2)));
        assignedOrder.setId(30L);
        assignedOrder.setAssignedEmployeeId(40L);
        assignedOrder.setStatus(OrderStatus.IN_PREPARATION);
        when(loggedUserPort.getUserId()).thenReturn(40L);
        when(loggedUserPort.getRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.assignPendingOrder(30L, 5L, 40L))
                .thenReturn(Optional.of(assignedOrder));

        Order result = useCase.assignOrder(30L);

        assertThat(result).isSameAs(assignedOrder);
        verify(traceabilityPort).registerStatusChange(assignedOrder, OrderStatus.PENDING, 40L);
    }

    @Test
    void rejectsUnavailableOrderWithoutRegisteringTraceability() {
        when(loggedUserPort.getUserId()).thenReturn(40L);
        when(loggedUserPort.getRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.assignPendingOrder(30L, 5L, 40L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.assignOrder(30L))
                .isInstanceOf(ValidationException.class);

        verify(traceabilityPort, never()).registerStatusChange(any(), any(), any());
    }

    private Order order(List<OrderItem> items) {
        return new Order(null, null, 5L, null, null, null, items);
    }

    private Dish dish(Long id, Long restaurantId, boolean active) {
        return new Dish(id, "Hamburguesa", 25000L, "Carne y queso",
                "https://cdn.example.com/dish.png", 2L, restaurantId, active);
    }
}
