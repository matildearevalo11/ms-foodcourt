package com.pragma.powerup.domain.spi;

public interface ICategoryPersistencePort {
    boolean existsById(Long categoryId);
}
