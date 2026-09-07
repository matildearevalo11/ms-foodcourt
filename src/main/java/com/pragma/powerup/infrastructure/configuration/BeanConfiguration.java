package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfiguration {
    @Bean
    IRestaurantServicePort restaurantServicePort(IRestaurantPersistencePort persistencePort,
                                                  IOwnerValidationPort ownerValidationPort) {
        return new RestaurantUseCase(persistencePort, ownerValidationPort);
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
                .build();
    }
}
