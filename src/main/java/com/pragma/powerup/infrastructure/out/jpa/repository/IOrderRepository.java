package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.domain.enums.OrderStatus;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface IOrderRepository extends JpaRepository<OrderEntity, Long> {
    boolean existsByCustomerIdAndStatusIn(Long customerId, Set<OrderStatus> statuses);
}
