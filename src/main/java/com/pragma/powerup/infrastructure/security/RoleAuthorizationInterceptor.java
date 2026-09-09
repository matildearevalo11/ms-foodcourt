package com.pragma.powerup.infrastructure.security;

import com.pragma.powerup.domain.exception.AuthenticationException;
import com.pragma.powerup.domain.exception.AuthorizationException;
import com.pragma.powerup.domain.exception.ExceptionMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requiredRole = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireRole.class);
        if (requiredRole == null) {
            requiredRole = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequireRole.class);
        }
        if (requiredRole == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException(ExceptionMessages.AUTHENTICATED_USER_NOT_FOUND.getMessage());
        }

        String expectedAuthority = ROLE_PREFIX + requiredRole.value().name();
        boolean authorized = authentication.getAuthorities().stream()
                .anyMatch(authority -> expectedAuthority.equals(authority.getAuthority()));
        if (!authorized) {
            throw new AuthorizationException(ExceptionMessages.ACCESS_DENIED.getMessage());
        }
        return true;
    }
}
