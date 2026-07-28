package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.List;

class RestaurantJpaAdapterTest {
    @Test
    void saveRestaurant_ShouldMapAndPersist() {
        IRestaurantRepository repository = mock(IRestaurantRepository.class);
        IRestaurantEntityMapper mapper = mock(IRestaurantEntityMapper.class);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mapper);
        Restaurant input = new Restaurant();
        Restaurant output = new Restaurant();
        RestaurantEntity entity = new RestaurantEntity();
        when(mapper.toEntity(input)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toRestaurant(entity)).thenReturn(output);

        assertSame(output, adapter.saveRestaurant(input));
    }

    @Test
    void existsByNit_ShouldDelegate() {
        IRestaurantRepository repository = mock(IRestaurantRepository.class);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mock(IRestaurantEntityMapper.class));
        when(repository.existsByNit("123")).thenReturn(true);
        assertTrue(adapter.existsByNit("123"));
    }

    @Test
    void existsById_ShouldDelegate() {
        IRestaurantRepository repository = mock(IRestaurantRepository.class);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mock(IRestaurantEntityMapper.class));
        when(repository.existsById(5L)).thenReturn(true);
        assertTrue(adapter.existsById(5L));
    }

    @Test
    void findById_WhenRestaurantExists_ShouldMapRestaurant() {
        IRestaurantRepository repository = mock(IRestaurantRepository.class);
        IRestaurantEntityMapper mapper = mock(IRestaurantEntityMapper.class);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mapper);
        RestaurantEntity entity = new RestaurantEntity();
        Restaurant restaurant = new Restaurant();
        when(repository.findById(5L)).thenReturn(Optional.of(entity));
        when(mapper.toRestaurant(entity)).thenReturn(restaurant);

        assertEquals(Optional.of(restaurant), adapter.findById(5L));
    }

    @Test
    void getAllByNameAsc_ShouldMapPageAndRequestCaseInsensitiveSorting() {
        IRestaurantRepository repository = mock(IRestaurantRepository.class);
        IRestaurantEntityMapper mapper = mock(IRestaurantEntityMapper.class);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mapper);
        RestaurantEntity entity = new RestaurantEntity();
        Restaurant restaurant = new Restaurant();
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toRestaurant(entity)).thenReturn(restaurant);

        var result = adapter.getAllByNameAsc(0, 5);

        assertEquals(List.of(restaurant), result.content());
        assertEquals(1, result.totalElements());
        var pageable = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageable.capture());
        assertEquals("name: ASC, ignoring case", pageable.getValue().getSort().toString());
    }
}
