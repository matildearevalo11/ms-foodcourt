package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.domain.exception.NotFoundException;
import com.pragma.powerup.domain.exception.ValidationException;
import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.Order;
import com.pragma.powerup.domain.model.OrderItem;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Employee;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import com.pragma.powerup.domain.spi.IPinGeneratorPort;
import com.pragma.powerup.domain.spi.IUserContactPort;
import com.pragma.powerup.domain.spi.INotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
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
    private IPinGeneratorPort pinGeneratorPort;
    private IUserContactPort userContactPort;
    private INotificationPort notificationPort;
    private Employee loggedEmployee;

    @BeforeEach
    void setUp() {
        orderPersistencePort = mock(IOrderPersistencePort.class);
        dishPersistencePort = mock(IDishPersistencePort.class);
        restaurantPersistencePort = mock(IRestaurantPersistencePort.class);
        loggedUserPort = mock(ILoggedUserPort.class);
        traceabilityPort = mock(ITraceabilityPort.class);
        pinGeneratorPort = mock(IPinGeneratorPort.class);
        userContactPort = mock(IUserContactPort.class);
        notificationPort = mock(INotificationPort.class);
        when(loggedUserPort.getLoggedUserId()).thenReturn(20L);
        loggedEmployee = new Employee(20L, "Ana Gómez");
        when(loggedUserPort.getLoggedEmployee()).thenReturn(loggedEmployee);
        useCase = new OrderUseCase(orderPersistencePort, dishPersistencePort,
                restaurantPersistencePort, loggedUserPort, traceabilityPort,
                pinGeneratorPort, userContactPort, notificationPort);
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
        verify(traceabilityPort).registerStatusChange(order, null, null);
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

    @Test
    void assignOrder_ShouldAssignLoggedEmployeeAndRegisterStatusChange() {
        Order assignedOrder = validOrder();
        assignedOrder.setId(25L);
        assignedOrder.setAssignedEmployeeId(20L);
        assignedOrder.setStatus(OrderStatus.IN_PREPARATION);
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.assignPendingOrder(25L, 5L, 20L))
                .thenReturn(Optional.of(assignedOrder));

        assertSame(assignedOrder, useCase.assignOrder(25L));
        verify(traceabilityPort).registerStatusChange(assignedOrder, OrderStatus.PENDING, loggedEmployee);
    }

    @Test
    void assignOrder_WhenOrderIsNotAvailable_ShouldFailWithoutTraceability() {
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.assignPendingOrder(25L, 5L, 20L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> useCase.assignOrder(25L));
        verifyNoInteractions(traceabilityPort);
    }

    @Test
    void markOrderReady_ShouldGeneratePinNotifyCustomerAndRegisterTraceability() {
        Order readyOrder = validOrder();
        readyOrder.setId(25L);
        readyOrder.setCustomerId(20L);
        readyOrder.setAssignedEmployeeId(30L);
        readyOrder.setStatus(OrderStatus.READY);
        when(loggedUserPort.getLoggedUserId()).thenReturn(30L);
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);
        when(pinGeneratorPort.generatePin()).thenReturn("482913");
        when(orderPersistencePort.markOrderReady(25L, 5L, 30L, "482913"))
                .thenReturn(Optional.of(readyOrder));
        when(userContactPort.getCellphone(20L)).thenReturn("+573001234567");

        assertSame(readyOrder, useCase.markOrderReady(25L));
        verify(traceabilityPort).registerStatusChange(readyOrder, OrderStatus.IN_PREPARATION, loggedEmployee);
        verify(notificationPort).notifyOrderReady("+573001234567", "482913");
    }

    @Test
    void markOrderReady_WhenOrderIsUnavailable_ShouldNotNotify() {
        when(loggedUserPort.getLoggedUserId()).thenReturn(30L);
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);
        when(pinGeneratorPort.generatePin()).thenReturn("482913");

        assertThrows(ValidationException.class, () -> useCase.markOrderReady(25L));
        verifyNoInteractions(userContactPort, notificationPort);
    }

    @Test
    void deliverOrder_WithValidPin_ShouldMarkDeliveredAndRegisterTraceability() {
        Order deliveredOrder = validOrder();
        deliveredOrder.setId(25L);
        deliveredOrder.setAssignedEmployeeId(30L);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);
        when(loggedUserPort.getLoggedUserId()).thenReturn(30L);
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);
        when(orderPersistencePort.deliverReadyOrder(25L, 5L, 30L, "482913"))
                .thenReturn(Optional.of(deliveredOrder));

        assertSame(deliveredOrder, useCase.deliverOrder(25L, "482913"));
        verify(traceabilityPort).registerStatusChange(deliveredOrder, OrderStatus.READY, loggedEmployee);
    }

    @Test
    void deliverOrder_WithInvalidStateEmployeeOrPin_ShouldFailWithoutTraceability() {
        when(loggedUserPort.getLoggedUserId()).thenReturn(30L);
        when(loggedUserPort.getLoggedRestaurantId()).thenReturn(5L);

        assertThrows(ValidationException.class, () -> useCase.deliverOrder(25L, "000000"));
        verifyNoInteractions(traceabilityPort);
    }

    @Test
    void cancelOrder_WhenPendingAndOwnedByCustomer_ShouldCancelAndRegisterTraceability() {
        Order canceledOrder = validOrder();
        canceledOrder.setId(25L);
        canceledOrder.setCustomerId(20L);
        canceledOrder.setStatus(OrderStatus.CANCELED);
        when(orderPersistencePort.cancelPendingOrder(25L, 20L)).thenReturn(Optional.of(canceledOrder));

        assertSame(canceledOrder, useCase.cancelOrder(25L));
        verify(traceabilityPort).registerStatusChange(canceledOrder, OrderStatus.PENDING, null);
    }

    @Test
    void cancelOrder_WhenNotPendingOrNotOwnedByCustomer_ShouldFailWithRequiredMessage() {
        ValidationException exception = assertThrows(ValidationException.class, () -> useCase.cancelOrder(25L));

        assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", exception.getMessage());
        verifyNoInteractions(traceabilityPort);
    }

    private Order validOrder() {
        return new Order(null, null, 5L, null, null, null,
                List.of(new OrderItem(null, 10L, 2), new OrderItem(null, 11L, 1)));
    }

    private Dish dish(Long id, Long restaurantId, boolean active) {
        return new Dish(id, "Dish", 10000L, "Description", "image", 2L, restaurantId, active);
    }
}
