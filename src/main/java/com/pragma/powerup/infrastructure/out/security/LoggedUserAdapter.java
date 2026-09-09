package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.exception.AuthenticationException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class LoggedUserAdapter implements ILoggedUserPort {
    @Override
    public Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationException(ExceptionMessages.AUTHENTICATED_USER_NOT_FOUND.getMessage());
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new AuthenticationException(ExceptionMessages.INVALID_AUTHENTICATED_USER_ID.getMessage());
        }
    }
}
