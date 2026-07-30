package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class LoggedUserAdapter implements ILoggedUserPort {

    @Override
    public Long getLoggedUserId() {
        return getRequiredLongClaim("userId", ExceptionMessages.INVALID_AUTHENTICATED_USER_ID);
    }

    @Override
    public Long getLoggedRestaurantId() {
        return getRequiredLongClaim("restaurantId", ExceptionMessages.EMPLOYEE_RESTAURANT_NOT_ASSIGNED);
    }

    private Long getRequiredLongClaim(String claimName, ExceptionMessages exceptionMessage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException(
                    ExceptionMessages.AUTHENTICATED_USER_NOT_FOUND.getMessage());
        }
        Object claim = jwt.getClaim(claimName);
        if (claim instanceof Number number) {
            return number.longValue();
        }
        throw new AuthenticationCredentialsNotFoundException(exceptionMessage.getMessage());
    }
}
