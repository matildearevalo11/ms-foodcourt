package com.pragma.powerup.domain.spi;

public interface IOwnerValidationPort {
    boolean isOwner(Long userId);
}
