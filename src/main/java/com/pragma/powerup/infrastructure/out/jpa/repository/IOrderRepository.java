package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;

public interface IOrderRepository extends JpaRepository<OrderEntity, Long> {
    boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses);
    Page<OrderEntity> findByRestaurant_IdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);
}
