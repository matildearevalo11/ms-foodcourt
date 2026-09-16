package com.pragma.powerup.infrastructure.out.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryJpaAdapterTest {
    @Mock
    ICategoryRepository repository;

    @Test
    void delegatesCategoryExistenceCheck() {
        when(repository.existsById(2L)).thenReturn(true);

        assertThat(new CategoryJpaAdapter(repository).existsById(2L)).isTrue();
    }
}
