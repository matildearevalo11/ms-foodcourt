package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

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
}
