package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

@RequiredArgsConstructor
public class DishJpaAdapter implements IDishPersistencePort {
    private final IDishRepository dishRepository;
    private final ICategoryRepository categoryRepository;
    private final IDishEntityMapper mapper;

    @Override
    public Dish saveDish(Dish dish) {
        return mapper.toDish(dishRepository.save(mapper.toEntity(dish)));
    }

    @Override
    public boolean categoryExistsById(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    @Override
    public Optional<Dish> findById(Long dishId) {
        return dishRepository.findById(dishId).map(mapper::toDish);
    }
}
