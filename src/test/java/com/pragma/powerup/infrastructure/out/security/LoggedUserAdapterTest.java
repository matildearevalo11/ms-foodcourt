package com.pragma.powerup.infrastructure.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.pragma.powerup.domain.exception.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class LoggedUserAdapterTest {
    private final LoggedUserAdapter adapter = new LoggedUserAdapter();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAuthenticatedUserIdentifier() {
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", "7", "restaurantId", 5L));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThat(adapter.getUserId()).isEqualTo(7L);
        assertThat(adapter.getRestaurantId()).isEqualTo(5L);
    }

    @Test
    void rejectsMissingOrInvalidIdentity() {
        assertThatThrownBy(adapter::getUserId)
                .isInstanceOf(AuthenticationException.class);

        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("sub", "invalid"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        assertThatThrownBy(adapter::getUserId)
                .isInstanceOf(AuthenticationException.class);
    }
}
