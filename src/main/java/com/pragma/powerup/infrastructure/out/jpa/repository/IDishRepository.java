package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDishRepository extends JpaRepository<DishEntity, Long> {
    Page<DishEntity> findByRestaurant_IdAndActiveTrue(Long restaurantId, Pageable pageable);
    Page<DishEntity> findByRestaurant_IdAndCategory_IdAndActiveTrue(
            Long restaurantId, Long categoryId, Pageable pageable);
}
