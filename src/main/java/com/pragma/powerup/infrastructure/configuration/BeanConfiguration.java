package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.domain.usecase.DishUseCase;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Configuration
public class BeanConfiguration {
    @Bean
    IRestaurantServicePort restaurantServicePort(IRestaurantPersistencePort persistencePort,      IOwnerValidationPort ownerValidationPort,
                                                  ILoggedUserPort loggedUserPort) {
        return new RestaurantUseCase(persistencePort, ownerValidationPort, loggedUserPort);
    }

    @Bean
    IDishServicePort dishServicePort(IDishPersistencePort dishPersistencePort, IRestaurantPersistencePort restaurantPersistencePort,
                                     ICategoryPersistencePort categoryPersistencePort, ILoggedUserPort loggedUserPort) {
        return new DishUseCase(dishPersistencePort, restaurantPersistencePort, categoryPersistencePort, loggedUserPort);
    }

    @Bean
    RestClient usersRestClient(@Value("${clients.users.base-url}") String baseUrl,
                               @Value("${clients.users.connect-timeout}") Duration connectTimeout,
                               @Value("${clients.users.read-timeout}") Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    if (SecurityContextHolder.getContext().getAuthentication() != null
                            && SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof Jwt jwt) {
                        request.getHeaders().setBearerAuth(jwt.getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
