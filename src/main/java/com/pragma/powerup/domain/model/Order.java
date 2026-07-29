package com.pragma.powerup.domain.model;

import com.pragma.powerup.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
}
