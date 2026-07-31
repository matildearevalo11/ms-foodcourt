package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.exception.ExceptionMessages;
import com.pragma.powerup.domain.spi.ILoggedUserPort;
import com.pragma.powerup.domain.model.Employee;
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

    @Override
    public Employee getLoggedEmployee() {
        Jwt jwt = getJwt();
        String name = jwt.getClaimAsString("name");
        String lastName = jwt.getClaimAsString("lastName");
        if (name == null || name.isBlank() || lastName == null || lastName.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException(
                    ExceptionMessages.AUTHENTICATED_EMPLOYEE_NAME_NOT_FOUND.getMessage());
        }
        return new Employee(getRequiredLongClaim(jwt, "userId",
                ExceptionMessages.INVALID_AUTHENTICATED_USER_ID),
                String.join(" ", name, lastName));
    }

    private Long getRequiredLongClaim(String claimName, ExceptionMessages exceptionMessage) {
        return getRequiredLongClaim(getJwt(), claimName, exceptionMessage);
    }

    private Long getRequiredLongClaim(Jwt jwt, String claimName, ExceptionMessages exceptionMessage) {
        Object claim = jwt.getClaim(claimName);
        if (claim instanceof Number number) {
            return number.longValue();
        }
        throw new AuthenticationCredentialsNotFoundException(exceptionMessage.getMessage());
    }

    private Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException(
                    ExceptionMessages.AUTHENTICATED_USER_NOT_FOUND.getMessage());
        }
        return jwt;
    }
}
