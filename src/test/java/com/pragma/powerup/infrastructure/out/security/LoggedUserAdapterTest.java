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

    private Jwt jwt(Object userId) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("owner@foodcourt.com")
                .claim("userId", userId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();
    }
}
