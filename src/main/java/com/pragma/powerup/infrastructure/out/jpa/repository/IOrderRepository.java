package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IOrderRepository extends JpaRepository<OrderEntity, Long> {
    boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses);

    Page<OrderEntity> findByRestaurant_IdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderEntity order
            SET order.assignedEmployeeId = :employeeId, order.status = :newStatus
            WHERE order.id = :orderId
              AND order.restaurant.id = :restaurantId
              AND order.status = :expectedStatus
              AND order.assignedEmployeeId IS NULL
            """)
    int assignIfAvailable(
            @Param("orderId") Long orderId,
            @Param("restaurantId") Long restaurantId,
            @Param("employeeId") Long employeeId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("newStatus") OrderStatus newStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderEntity order
            SET order.securityPin = :securityPin, order.status = :newStatus
            WHERE order.id = :orderId
              AND order.restaurant.id = :restaurantId
              AND order.assignedEmployeeId = :employeeId
              AND order.status = :expectedStatus
            """)
    int markReadyIfAssigned(
            @Param("orderId") Long orderId,
            @Param("restaurantId") Long restaurantId,
            @Param("employeeId") Long employeeId,
            @Param("securityPin") String securityPin,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("newStatus") OrderStatus newStatus);
}
