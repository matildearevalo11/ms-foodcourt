package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

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

    @Test
    void findById_WhenDishExists_ShouldMapDish() {
        IDishRepository repository = mock(IDishRepository.class);
        IDishEntityMapper mapper = mock(IDishEntityMapper.class);
        DishJpaAdapter adapter = new DishJpaAdapter(repository, mock(ICategoryRepository.class), mapper);
        DishEntity entity = new DishEntity();
        Dish dish = new Dish();
        when(repository.findById(10L)).thenReturn(Optional.of(entity));
        when(mapper.toDish(entity)).thenReturn(dish);

        assertEquals(Optional.of(dish), adapter.findById(10L));
    }

    @Test
    void findById_WhenDishDoesNotExist_ShouldReturnEmpty() {
        IDishRepository repository = mock(IDishRepository.class);
        DishJpaAdapter adapter = new DishJpaAdapter(repository, mock(ICategoryRepository.class),
                mock(IDishEntityMapper.class));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(adapter.findById(99L).isEmpty());
    }
}
