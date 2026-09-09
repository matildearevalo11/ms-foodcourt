package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DishJpaAdapterTest {
    @Mock
    IDishRepository repository;

    @Mock
    IDishEntityMapper mapper;

    @Test
    void delegatesDishPersistence() {
        Dish dish = new Dish();
        Dish saved = new Dish();
        DishEntity entity = new DishEntity();
        when(mapper.toEntity(dish)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(saved);

        DishJpaAdapter adapter = new DishJpaAdapter(repository, mapper);

        assertThat(adapter.save(dish)).isSameAs(saved);
    }

    @Test
    void findsAndMapsDish() {
        DishEntity entity = new DishEntity();
        Dish dish = new Dish();
        when(repository.findById(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(dish);

        DishJpaAdapter adapter = new DishJpaAdapter(repository, mapper);

        assertThat(adapter.findById(10L)).containsSame(dish);
    }
}
