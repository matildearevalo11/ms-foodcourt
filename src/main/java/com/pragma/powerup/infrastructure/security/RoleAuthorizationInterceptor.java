package com.pragma.powerup.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
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
        RequireRole requiredRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requiredRole == null) {
            requiredRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requiredRole == null) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authority = ROLE_PREFIX + requiredRole.value().getName();
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()))) {
            throw new AccessDeniedException("Access denied");
        }
        return true;
    }
}
