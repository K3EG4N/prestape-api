package com.prestape.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

public class IncompleteProfileFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH = "/api/auth/";
    private static final String ME_PATH = "/api/me";
    private static final String MENU_PATH = "/api/menus";
    private static final String COMPLETE_PROFILE_PATH = "/api/me/profile";
    private static final String MESSAGE = "Completa tu perfil para continuar";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (hasIncompleteProfile() && !isAllowed(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"status\":403,\"success\":false,\"code\":\"PROFILE_INCOMPLETE\",\"message\":\"" + MESSAGE + "\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean hasIncompleteProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.getPrincipal() instanceof CustomUserDetails user
                && !user.isProfileCompleted();
    }

    private boolean isAllowed(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return "OPTIONS".equals(method)
                || path.startsWith(AUTH_PATH)
                || ("GET".equals(method) && (path.equals(ME_PATH) || path.equals(MENU_PATH)))
                || ("POST".equals(method) && path.equals(COMPLETE_PROFILE_PATH));
    }
}
