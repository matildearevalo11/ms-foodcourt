package com.pragma.powerup.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING(true),
    IN_PREPARATION(true),
    READY(true),
    DELIVERED(false),
    CANCELED(false);

    private static final Set<OrderStatus> ACTIVE_STATUSES = Arrays.stream(values())
            .filter(OrderStatus::isActive)
            .collect(Collectors.toUnmodifiableSet());

    private final boolean active;

    public static Set<OrderStatus> activeStatuses() {
        return ACTIVE_STATUSES;
    }
}
