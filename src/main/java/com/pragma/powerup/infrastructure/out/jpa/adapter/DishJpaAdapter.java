package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DishJpaAdapter implements IDishPersistencePort {
    private final IDishRepository repository;
    private final IDishEntityMapper mapper;

    @Override
    public Dish save(Dish dish) {
        return mapper.toDomain(repository.save(mapper.toEntity(dish)));
    }

    @Override
    public Optional<Dish> findById(Long dishId) {
        return repository.findById(dishId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Dish> findActiveByRestaurant(Long restaurantId, Long categoryId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<DishEntity> result = categoryId == null ? repository.findByRestaurantIdAndActiveTrue(restaurantId, pageRequest)
                : repository.findByRestaurantIdAndCategoryIdAndActiveTrue(restaurantId, categoryId, pageRequest);
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public List<Dish> findAllById(Set<Long> dishIds) {
        return repository.findAllById(dishIds).stream().map(mapper::toDomain).toList();
    }
}
