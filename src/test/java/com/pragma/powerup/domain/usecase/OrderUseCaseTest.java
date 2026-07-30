package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderUseCaseTest {
    private IOrderPersistencePort orderPersistencePort;
    private IDishPersistencePort dishPersistencePort;
    private IRestaurantPersistencePort restaurantPersistencePort;
    private ILoggedUserPort loggedUserPort;
    private OrderUseCase useCase;
    private ITraceabilityPort traceabilityPort;

    @BeforeEach
    void setUp() {
        orderPersistencePort = mock(IOrderPersistencePort.class);
        dishPersistencePort = mock(IDishPersistencePort.class);
        restaurantPersistencePort = mock(IRestaurantPersistencePort.class);
        loggedUserPort = mock(ILoggedUserPort.class);
        traceabilityPort = mock(ITraceabilityPort.class);
        when(loggedUserPort.getLoggedUserId()).thenReturn(20L);
        useCase = new OrderUseCase(orderPersistencePort, dishPersistencePort,
                restaurantPersistencePort, loggedUserPort, traceabilityPort);
    }

    @Test
    void saveOrder_WithValidItems_ShouldCreatePendingOrderForLoggedCustomer() {
        Order order = validOrder();
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
        when(dishPersistencePort.findAllByIds(Set.of(10L, 11L))).thenReturn(List.of(
                dish(10L, 5L, true), dish(11L, 5L, true)));
        when(orderPersistencePort.saveOrder(order)).thenReturn(order);

        Order result = useCase.saveOrder(order);

        assertEquals(20L, result.getCustomerId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(orderPersistencePort).saveOrder(order);
        verify(traceabilityPort).registerStatusChange(order, null);
    }

    @Test
    void saveOrder_WhenCustomerHasActiveOrder_ShouldFail() {
        Order order = validOrder();
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
        when(dishPersistencePort.findAllByIds(Set.of(10L, 11L))).thenReturn(List.of(
                dish(10L, 5L, true), dish(11L, 5L, true)));
        when(orderPersistencePort.existsByCustomerIdAndStatusIn(eq(20L), anySet())).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.saveOrder(order));
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void saveOrder_WhenRestaurantDoesNotExist_ShouldFail() {
        assertThrows(NotFoundException.class, () -> useCase.saveOrder(validOrder()));
        verifyNoInteractions(dishPersistencePort);
        verify(orderPersistencePort, never()).saveOrder(any());
    }

    @Test
    void saveOrder_WhenDishBelongsToAnotherRestaurant_ShouldFail() {
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
        when(dishPersistencePort.findAllByIds(Set.of(10L, 11L))).thenReturn(List.of(
                dish(10L, 5L, true), dish(11L, 8L, true)));

        assertThrows(ValidationException.class, () -> useCase.saveOrder(validOrder()));
    }

    @Test
    void saveOrder_WhenDishIsInactive_ShouldFail() {
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);
        when(dishPersistencePort.findAllByIds(Set.of(10L, 11L))).thenReturn(List.of(
                dish(10L, 5L, true), dish(11L, 5L, false)));

        assertThrows(ValidationException.class, () -> useCase.saveOrder(validOrder()));
    }

    @Test
    void saveOrder_WithDuplicatedDish_ShouldFail() {
        Order order = validOrder();
        order.setItems(List.of(new OrderItem(null, 10L, 1), new OrderItem(null, 10L, 2)));
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.saveOrder(order));
        verifyNoInteractions(dishPersistencePort);
    }

    @Test
    void saveOrder_WithEmptyItems_ShouldFail() {
        Order order = validOrder();
        order.setItems(List.of());
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.saveOrder(order));
    }

    @Test
    void saveOrder_WithInvalidQuantity_ShouldFail() {
        Order order = validOrder();
        order.getItems().getFirst().setQuantity(0);
        when(restaurantPersistencePort.existsById(5L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> useCase.saveOrder(order));
    }

    @Test
    void getOrders_ShouldUseRestaurantFromLoggedEmployee() {
        PageResult<Order> expected = new PageResult<>(List.of(validOrder()), 0, 10, 1, 1);
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.findByRestaurantIdAndStatus(5L, OrderStatus.PENDING, 0, 10))
                .thenReturn(expected);

        assertSame(expected, useCase.getOrders(OrderStatus.PENDING, 0, 10));
    }

    private Order validOrder() {
        return new Order(null, null, 5L, null, null, null,
                List.of(new OrderItem(null, 10L, 2), new OrderItem(null, 11L, 1)));
    }

    private Dish dish(Long id, Long restaurantId, boolean active) {
        return new Dish(id, "Dish", 10000L, "Description", "image", 2L, restaurantId, active);
    }
}
