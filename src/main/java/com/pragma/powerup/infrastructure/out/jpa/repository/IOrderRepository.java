package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;

public interface IOrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses);

    Page<OrderEntity> findByRestaurant_IdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderEntity o
            SET o.assignedEmployeeId = :employeeId, o.status = :newStatus
            WHERE o.id = :orderId
              AND o.restaurant.id = :restaurantId
              AND o.status = :expectedStatus
              AND o.assignedEmployeeId IS NULL
            """)
    int assignIfAvailable(@Param("orderId") Long orderId, @Param("restaurantId") Long restaurantId, @Param("employeeId") Long employeeId,
                          @Param("expectedStatus") OrderStatus expectedStatus, @Param("newStatus") OrderStatus newStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderEntity o
            SET o.securityPin = :securityPin, o.status = :newStatus
            WHERE o.id = :orderId
              AND o.restaurant.id = :restaurantId
              AND o.assignedEmployeeId = :employeeId
              AND o.status = :expectedStatus
            """)
    int markReadyIfAssigned(@Param("orderId") Long orderId, @Param("restaurantId") Long restaurantId,
                            @Param("employeeId") Long employeeId, @Param("securityPin") String securityPin,
                            @Param("expectedStatus") OrderStatus expectedStatus, @Param("newStatus") OrderStatus newStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderEntity o
            SET o.status = :newStatus, o.securityPin = NULL
            WHERE o.id = :orderId
              AND o.restaurant.id = :restaurantId
              AND o.assignedEmployeeId = :employeeId
              AND o.status = :expectedStatus
              AND o.securityPin = :securityPin
            """)
    int deliverIfReadyAndPinMatches(@Param("orderId") Long orderId, @Param("restaurantId") Long restaurantId,
                                    @Param("employeeId") Long employeeId, @Param("securityPin") String securityPin,
                                    @Param("expectedStatus") OrderStatus expectedStatus,
                                    @Param("newStatus") OrderStatus newStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderEntity o
            SET o.status = :newStatus
            WHERE o.id = :orderId
              AND o.customerId = :customerId
              AND o.status = :expectedStatus
            """)
    int cancelIfPending(@Param("orderId") Long orderId, @Param("customerId") Long customerId,
                        @Param("expectedStatus") OrderStatus expectedStatus, @Param("newStatus") OrderStatus newStatus);
}
