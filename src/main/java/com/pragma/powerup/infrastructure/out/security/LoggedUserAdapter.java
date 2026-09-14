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
        return getLongClaim(getJwt().getSubject(), ExceptionMessages.INVALID_AUTHENTICATED_USER_ID);
    }

    @Override
    public Long getRestaurantId() {
        Object restaurantId = getJwt().getClaim("restaurantId");
        if (restaurantId instanceof Number number) {
            return number.longValue();
        }
        throw new AuthenticationException(ExceptionMessages.EMPLOYEE_RESTAURANT_NOT_ASSIGNED.getMessage());
    }

    private Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationException(ExceptionMessages.AUTHENTICATED_USER_NOT_FOUND.getMessage());
        }
        return jwt;
    }

    private Long getLongClaim(String value, ExceptionMessages exceptionMessage) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new AuthenticationException(exceptionMessage.getMessage());
        }
    }
}
