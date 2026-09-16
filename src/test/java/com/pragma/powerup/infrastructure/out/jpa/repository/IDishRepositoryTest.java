package com.pragma.powerup.infrastructure.out.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.pragma.powerup.infrastructure.out.jpa.entity.CategoryEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest(properties = "spring.sql.init.mode=never")
class IDishRepositoryTest {
    @Autowired
    IDishRepository dishRepository;

    @Autowired
    ICategoryRepository categoryRepository;

    @Autowired
    IRestaurantRepository restaurantRepository;

    @Test
    void filtersActiveDishesByRestaurantAndOptionalCategoryWithPagination() {
        RestaurantEntity restaurant = restaurantRepository.save(restaurant("9001"));
        RestaurantEntity anotherRestaurant = restaurantRepository.save(restaurant("9002"));
        CategoryEntity burgers = categoryRepository.save(category(1L, "Hamburguesas"));
        CategoryEntity drinks = categoryRepository.save(category(2L, "Bebidas"));

        dishRepository.save(dish("Hamburguesa", restaurant, burgers, true));
        dishRepository.save(dish("Gaseosa", restaurant, drinks, true));
        dishRepository.save(dish("Plato inactivo", restaurant, burgers, false));
        dishRepository.save(dish("Otro restaurante", anotherRestaurant, burgers, true));

        Page<DishEntity> menu = dishRepository.findByRestaurantIdAndActiveTrue(
                restaurant.getId(), PageRequest.of(0, 1));
        Page<DishEntity> filtered = dishRepository.findByRestaurantIdAndCategoryIdAndActiveTrue(
                restaurant.getId(), burgers.getId(), PageRequest.of(0, 10));

        assertThat(menu.getTotalElements()).isEqualTo(2);
        assertThat(menu.getTotalPages()).isEqualTo(2);
        assertThat(filtered.getContent()).extracting(DishEntity::getName)
                .containsExactly("Hamburguesa");
    }

    private RestaurantEntity restaurant(String nit) {
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setName("Restaurante " + nit);
        restaurant.setNit(nit);
        restaurant.setAddress("Local 1");
        restaurant.setPhone("3001234567");
        restaurant.setUrlLogo("https://cdn.example.com/logo.png");
        restaurant.setOwnerId(7L);
        return restaurant;
    }

    private CategoryEntity category(Long id, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private DishEntity dish(String name, RestaurantEntity restaurant, CategoryEntity category, boolean active) {
        DishEntity dish = new DishEntity();
        dish.setName(name);
        dish.setPrice(25000L);
        dish.setDescription("Descripción");
        dish.setUrlImage("https://cdn.example.com/dish.png");
        dish.setRestaurant(restaurant);
        dish.setCategory(category);
        dish.setActive(active);
        return dish;
    }
}
