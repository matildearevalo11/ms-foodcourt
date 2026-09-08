package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantJpaAdapterTest {
    @Mock
    IRestaurantRepository repository;

    @Mock
    IRestaurantEntityMapper mapper;

    @Test
    void delegatesSaveAndNitLookup() {
        Restaurant restaurant = new Restaurant();
        Restaurant saved = new Restaurant();
        RestaurantEntity entity = new RestaurantEntity();
        when(mapper.toEntity(restaurant)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);
        when(repository.existsByNit("9001")).thenReturn(true);
        when(repository.existsById(5L)).thenReturn(true);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mapper);

        assertThat(adapter.save(restaurant)).isSameAs(saved);
        assertThat(adapter.existsByNit("9001")).isTrue();
        assertThat(adapter.existsById(5L)).isTrue();
    }
}
