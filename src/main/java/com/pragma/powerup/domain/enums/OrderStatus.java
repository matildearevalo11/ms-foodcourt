package com.pragma.powerup.domain.enums;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING,
    IN_PREPARATION,
    READY,
    DELIVERED,
    CANCELED;

    private static final Set<OrderStatus> ACTIVE_STATUSES =
            Set.copyOf(EnumSet.of(PENDING, IN_PREPARATION, READY));

    public static Set<OrderStatus> activeStatuses() {
        return ACTIVE_STATUSES;
    }

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(this);
    }
}
