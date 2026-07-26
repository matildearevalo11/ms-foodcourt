package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DishJpaAdapterTest {
    @Test
    void saveDish_ShouldMapAndPersist() {
        IDishRepository repository = mock(IDishRepository.class);
        ICategoryRepository categories = mock(ICategoryRepository.class);
        IDishEntityMapper mapper = mock(IDishEntityMapper.class);
        DishJpaAdapter adapter = new DishJpaAdapter(repository, categories, mapper);
        Dish dish = new Dish();
        DishEntity entity = new DishEntity();
        Dish saved = new Dish();
        when(mapper.toEntity(dish)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDish(entity)).thenReturn(saved);
        assertSame(saved, adapter.saveDish(dish));
    }

    @Test
    void categoryExistsById_ShouldDelegate() {
        ICategoryRepository categories = mock(ICategoryRepository.class);
        DishJpaAdapter adapter = new DishJpaAdapter(mock(IDishRepository.class), categories,
                mock(IDishEntityMapper.class));
        when(categories.existsById(2L)).thenReturn(true);
        assertTrue(adapter.categoryExistsById(2L));
    }
}
