package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.pragma.powerup.domain.model.Dish;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
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

    @Test
    void mapsFilteredDishPageAndPreservesPaginationMetadata() {
        DishEntity entity = new DishEntity();
        Dish dish = new Dish();
        PageRequest pageRequest = PageRequest.of(1, 5);
        when(repository.findByRestaurantIdAndCategoryIdAndActiveTrue(
                eq(5L), eq(2L), argThat(pageable -> pageable.getPageNumber() == 1
                        && pageable.getPageSize() == 5)))
                .thenReturn(new PageImpl<>(List.of(entity), pageRequest, 6));
        when(mapper.toDomain(entity)).thenReturn(dish);

        DishJpaAdapter adapter = new DishJpaAdapter(repository, mapper);

        var result = adapter.findActiveByRestaurant(5L, 2L, 1, 5);

        assertThat(result.content()).containsExactly(dish);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(6);
        assertThat(result.totalPages()).isEqualTo(2);
    }
}
