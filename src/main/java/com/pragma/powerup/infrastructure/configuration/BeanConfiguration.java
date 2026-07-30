package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import com.pragma.powerup.domain.usecase.DishUseCase;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.domain.usecase.OrderUseCase;
import com.pragma.powerup.infrastructure.out.jpa.adapter.DishJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.RestaurantJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.adapter.OrderJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IRestaurantEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.ICategoryRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IRestaurantRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.rest.adapter.UserServiceRestAdapter;
import com.pragma.powerup.infrastructure.out.rest.adapter.TraceabilityRestAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
        return builder.baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    if (SecurityContextHolder.getContext().getAuthentication() != null
                            && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
                        request.getHeaders().setBearerAuth(jwt.getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    public IOwnerValidationPort ownerValidationPort(RestClient usersRestClient) {
        return new UserServiceRestAdapter(usersRestClient);
    }

    @Bean
    public RestClient traceabilityRestClient(RestClient.Builder builder,
                                             @Value("${clients.traceability.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl)
                .requestInterceptor((request, body, execution) -> {
                    if (SecurityContextHolder.getContext().getAuthentication() != null
                            && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
                        request.getHeaders().setBearerAuth(jwt.getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    public ITraceabilityPort traceabilityPort(RestClient traceabilityRestClient) {
        return new TraceabilityRestAdapter(traceabilityRestClient);
    }

    @Bean
    public IRestaurantServicePort restaurantServicePort(IRestaurantPersistencePort persistencePort,
                                     IOwnerValidationPort ownerValidationPort, ILoggedUserPort loggedUserPort) {
        return new RestaurantUseCase(persistencePort, ownerValidationPort, loggedUserPort);
    }

    @Bean
    public IDishPersistencePort dishPersistencePort(IDishRepository dishRepository, ICategoryRepository categoryRepository,
                                                     IDishEntityMapper mapper) {
        return new DishJpaAdapter(dishRepository, categoryRepository, mapper);
    }

    @Bean
    public IDishServicePort dishServicePort(IDishPersistencePort dishPersistencePort, IRestaurantPersistencePort restaurantPersistencePort,
                                             ILoggedUserPort loggedUserPort) {
        return new DishUseCase(dishPersistencePort, restaurantPersistencePort, loggedUserPort);
    }

    @Bean
    public IOrderPersistencePort orderPersistencePort(IOrderRepository orderRepository,
                                                       IOrderItemRepository orderItemRepository,
                                                       IOrderEntityMapper orderMapper,
                                                       IOrderItemEntityMapper orderItemMapper) {
        return new OrderJpaAdapter(orderRepository, orderItemRepository, orderMapper, orderItemMapper);
    }

    @Bean
    public IOrderServicePort orderServicePort(IOrderPersistencePort orderPersistencePort, IDishPersistencePort dishPersistencePort,
                                               IRestaurantPersistencePort restaurantPersistencePort, ILoggedUserPort loggedUserPort,
                                               ITraceabilityPort traceabilityPort) {
        return new OrderUseCase(orderPersistencePort, dishPersistencePort, restaurantPersistencePort, loggedUserPort, traceabilityPort);
    }

}
