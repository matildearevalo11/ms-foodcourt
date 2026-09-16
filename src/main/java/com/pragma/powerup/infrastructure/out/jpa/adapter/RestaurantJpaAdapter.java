package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.PageResult;
import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantJpaAdapter implements IRestaurantPersistencePort {
    private final IRestaurantRepository repository;
    private final IRestaurantEntityMapper mapper;

    @Override
    public Restaurant save(Restaurant restaurant) {
        return mapper.toDomain(repository.save(mapper.toEntity(restaurant)));
    }

    @Override
    public boolean existsByNit(String nit) {
        return repository.existsByNit(nit);
    }

    @Override
    public Optional<Restaurant> findById(Long restaurantId) {
        return repository.findById(restaurantId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Restaurant> findAllByNameAsc(int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.asc("id"));
        Page<RestaurantEntity> result = repository.findAll(PageRequest.of(page, size, sort));
        List<Restaurant> restaurants = result.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(restaurants, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
}
