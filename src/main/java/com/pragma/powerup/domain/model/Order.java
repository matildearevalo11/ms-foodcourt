package com.pragma.powerup.domain.model;

import com.pragma.powerup.domain.enums.OrderStatus;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long assignedEmployeeId;
    private OrderStatus status;
    private Instant createdAt;
    private List<OrderItem> items;
}
