package com.pragma.powerup.infrastructure.out.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoggedUserAdapterTest {

    private final LoggedUserAdapter adapter = new LoggedUserAdapter();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getLoggedUserId_WithValidatedJwt_ShouldReturnSubject() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(7L)));

        assertEquals(7L, adapter.getLoggedUserId());
    }

    @Test
    void getLoggedUserId_WithoutAuthentication_ShouldFail() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, adapter::getLoggedUserId);
    }

    @Test
    void getLoggedUserId_WithInvalidSubject_ShouldFail() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt("invalid")));

        assertThrows(AuthenticationCredentialsNotFoundException.class, adapter::getLoggedUserId);
    }

    @Test
    void getLoggedRestaurantId_WithEmployeeJwt_ShouldReturnRestaurant() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(30L, 5L)));

        assertEquals(5L, adapter.getLoggedRestaurantId());
    }

    @Test
    void getLoggedRestaurantId_WithoutClaim_ShouldFail() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt(30L)));

        assertThrows(AuthenticationCredentialsNotFoundException.class, adapter::getLoggedRestaurantId);
    }

    private Jwt jwt(Object userId) {
        return jwt(userId, null);
    }

    private Jwt jwt(Object userId, Long restaurantId) {
        Instant now = Instant.now();
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("owner@foodcourt.com")
                .claim("userId", userId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300));
        if (restaurantId != null) {
            builder.claim("restaurantId", restaurantId);
        }
        return builder.build();
    }
}
