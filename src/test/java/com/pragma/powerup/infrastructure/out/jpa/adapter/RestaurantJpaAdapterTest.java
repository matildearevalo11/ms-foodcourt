package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pragma.powerup.domain.model.Restaurant;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
        when(repository.findById(5L)).thenReturn(Optional.of(entity));
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mapper);

        assertThat(adapter.save(restaurant)).isSameAs(saved);
        assertThat(adapter.existsByNit("9001")).isTrue();
        assertThat(adapter.findById(5L)).containsSame(saved);
    }

    @Test
    void returnsMappedPageWithStableAlphabeticalSorting() {
        RestaurantEntity entity = new RestaurantEntity();
        Restaurant restaurant = new Restaurant();
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.toDomain(entity)).thenReturn(restaurant);
        RestaurantJpaAdapter adapter = new RestaurantJpaAdapter(repository, mapper);

        var result = adapter.findAllByNameAsc(0, 5);

        assertThat(result.content()).containsExactly(restaurant);
        assertThat(result.totalElements()).isEqualTo(1);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageable.capture());
        assertThat(pageable.getValue().getSort().toString())
                .isEqualTo("name: ASC, ignoring case,id: ASC");
    }
}
