package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.Optional;

@RequiredArgsConstructor
public class RestaurantJpaAdapter implements IRestaurantPersistencePort {
    private final IRestaurantRepository repository;
    private final IRestaurantEntityMapper mapper;

    @Override
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return mapper.toRestaurant(repository.save(mapper.toEntity(restaurant)));
    }

    @Override
    public boolean existsByNit(String nit) {
        return repository.existsByNit(nit);
    }

    @Override
    public boolean existsById(Long restaurantId) {
        return repository.existsById(restaurantId);
    }

    @Override
    public Optional<Restaurant> findById(Long restaurantId) {
        return repository.findById(restaurantId).map(mapper::toRestaurant);
    }

    @Override
    public PageResult<Restaurant> findAllByNameAsc(int page, int size) {
        Page<RestaurantEntity> result = repository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Order.asc("name").ignoreCase())));
        return new PageResult<>(result.getContent().stream().map(mapper::toRestaurant).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

}
