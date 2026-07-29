package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Override
    public PageResult<Dish> findActiveDishes(Long restaurantId, Long categoryId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<DishEntity> result = categoryId == null
                ? dishRepository.findByRestaurant_IdAndActiveTrue(restaurantId, pageable)
                : dishRepository.findByRestaurant_IdAndCategory_IdAndActiveTrue(restaurantId, categoryId, pageable);
        return new PageResult<>(result.getContent().stream().map(mapper::toDish).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
