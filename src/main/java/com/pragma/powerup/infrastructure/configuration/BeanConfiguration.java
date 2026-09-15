package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.ICategoryPersistencePort;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IOwnerValidationPort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ITraceabilityPort;
import com.pragma.powerup.domain.spi.INotificationPort;
import com.pragma.powerup.domain.spi.IPinGeneratorPort;
import com.pragma.powerup.domain.spi.IPinHashingPort;
import com.pragma.powerup.domain.spi.IUserContactPort;
import com.pragma.powerup.domain.usecase.DishUseCase;
import com.pragma.powerup.domain.usecase.OrderUseCase;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.infrastructure.out.jpa.adapter.OrderJpaAdapter;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderItemEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderItemRepository;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import com.pragma.powerup.infrastructure.out.rest.adapter.TraceabilityRestAdapter;
import com.pragma.powerup.infrastructure.out.rest.adapter.NotificationRestAdapter;
import com.pragma.powerup.infrastructure.out.rest.adapter.UserContactRestAdapter;
import com.pragma.powerup.infrastructure.out.security.SecurePinGeneratorAdapter;
import com.pragma.powerup.infrastructure.out.security.HmacPinHashingAdapter;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfiguration {
    @Bean
    IRestaurantServicePort restaurantServicePort(IRestaurantPersistencePort persistencePort,
            IOwnerValidationPort ownerValidationPort, ILoggedUserPort loggedUserPort) {
        return new RestaurantUseCase(persistencePort, ownerValidationPort, loggedUserPort);
    }

    @Bean
    IDishServicePort dishServicePort(IDishPersistencePort dishPersistencePort, IRestaurantPersistencePort restaurantPersistencePort,
                                     ICategoryPersistencePort categoryPersistencePort, ILoggedUserPort loggedUserPort) {
        return new DishUseCase(dishPersistencePort, restaurantPersistencePort, categoryPersistencePort, loggedUserPort);
    }

    @Bean
    @Primary
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

    @Bean
    IOrderPersistencePort orderPersistencePort(IOrderRepository orderRepository,
            IOrderItemRepository itemRepository, IOrderEntityMapper orderMapper,
            IOrderItemEntityMapper itemMapper) {
        return new OrderJpaAdapter(orderRepository, itemRepository, orderMapper, itemMapper);
    }

    @Bean
    IOrderServicePort orderServicePort(IOrderPersistencePort orderPersistencePort,
            IDishPersistencePort dishPersistencePort, IRestaurantPersistencePort restaurantPersistencePort,
            ILoggedUserPort loggedUserPort, ITraceabilityPort traceabilityPort,
            IPinGeneratorPort pinGeneratorPort, IPinHashingPort pinHashingPort, IUserContactPort userContactPort,
            INotificationPort notificationPort) {
        return new OrderUseCase(orderPersistencePort, dishPersistencePort, restaurantPersistencePort,
                loggedUserPort, traceabilityPort, pinGeneratorPort, pinHashingPort, userContactPort, notificationPort);
    }

    @Bean
    RestClient traceabilityRestClient(@Value("${clients.traceability.base-url}") String baseUrl,
            @Value("${clients.traceability.api-key}") String apiKey,
            @Value("${clients.traceability.connect-timeout}") Duration connectTimeout,
            @Value("${clients.traceability.read-timeout}") Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }

    @Bean
    ITraceabilityPort traceabilityPort(
            @Qualifier("traceabilityRestClient") RestClient traceabilityRestClient) {
        return new TraceabilityRestAdapter(traceabilityRestClient);
    }

    @Bean
    RestClient userContactRestClient(@Value("${clients.users.base-url}") String baseUrl,
            @Value("${clients.users.api-key}") String apiKey,
            @Value("${clients.users.connect-timeout}") Duration connectTimeout,
            @Value("${clients.users.read-timeout}") Duration readTimeout) {
        return internalRestClient(baseUrl, apiKey, connectTimeout, readTimeout);
    }

    @Bean
    IUserContactPort userContactPort(
            @Qualifier("userContactRestClient") RestClient userContactRestClient) {
        return new UserContactRestAdapter(userContactRestClient);
    }

    @Bean
    RestClient messagingRestClient(@Value("${clients.messaging.base-url}") String baseUrl,
            @Value("${clients.messaging.api-key}") String apiKey,
            @Value("${clients.messaging.connect-timeout}") Duration connectTimeout,
            @Value("${clients.messaging.read-timeout}") Duration readTimeout) {
        return internalRestClient(baseUrl, apiKey, connectTimeout, readTimeout);
    }

    @Bean
    INotificationPort notificationPort(
            @Qualifier("messagingRestClient") RestClient messagingRestClient) {
        return new NotificationRestAdapter(messagingRestClient);
    }

    @Bean
    IPinGeneratorPort pinGeneratorPort() {
        return new SecurePinGeneratorAdapter(new SecureRandom());
    }

    @Bean
    IPinHashingPort pinHashingPort(@Value("${security.order-pin-secret}") String secret) {
        return new HmacPinHashingAdapter(secret);
    }

    private RestClient internalRestClient(String baseUrl, String apiKey,
            Duration connectTimeout, Duration readTimeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Internal-Api-Key", apiKey)
                .build();
    }
}
