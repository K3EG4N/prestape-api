package com.prestape.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.prestape.dto.CustomUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SuspendedAccountFilter extends OncePerRequestFilter {

    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final String AUTH_PATH = "/api/auth/";
    private static final String MESSAGE = "Tu cuenta está suspendida. Solo puedes consultar información";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (isSuspended() && !isAllowed(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"status\":403,\"success\":false,\"code\":\"ACCOUNT_SUSPENDED\",\"message\":\"" + MESSAGE + "\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isSuspended() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.getPrincipal() instanceof CustomUserDetails user
                && user.isSuspended();
    }

    private boolean isAllowed(HttpServletRequest request) {
        return READ_METHODS.contains(request.getMethod())
                || request.getRequestURI().startsWith(AUTH_PATH);
    }
}
