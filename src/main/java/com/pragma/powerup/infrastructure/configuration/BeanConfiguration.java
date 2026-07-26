package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.usecase.DishUseCase;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.infrastructure.out.jpa.adapter.DishJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.RestaurantJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import com.pragma.powerup.infrastructure.out.rest.adapter.UserServiceRestAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfiguration {
    @Bean
    public IRestaurantPersistencePort restaurantPersistencePort(
            IRestaurantRepository repository, IRestaurantEntityMapper mapper) {
        return new RestaurantJpaAdapter(repository, mapper);
    }

    @Bean
    public RestClient usersRestClient(RestClient.Builder builder,
                                      @Value("${clients.users.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public IOwnerValidationPort ownerValidationPort(RestClient usersRestClient) {
        return new UserServiceRestAdapter(usersRestClient);
    }

    @Bean
    public IRestaurantServicePort restaurantServicePort(
            IRestaurantPersistencePort persistencePort, IOwnerValidationPort ownerValidationPort) {
        return new RestaurantUseCase(persistencePort, ownerValidationPort);
    }

    @Bean
    public IDishPersistencePort dishPersistencePort(IDishRepository dishRepository, ICategoryRepository categoryRepository,
                                                     IDishEntityMapper mapper) {
        return new DishJpaAdapter(dishRepository, categoryRepository, mapper);
    }

    @Bean
    public IDishServicePort dishServicePort(IDishPersistencePort dishPersistencePort,
                                             IRestaurantPersistencePort restaurantPersistencePort) {
        return new DishUseCase(dishPersistencePort, restaurantPersistencePort);
    }

}
