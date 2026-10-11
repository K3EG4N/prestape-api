package com.prestape.controller;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.request.LoginRequest;
import com.prestape.dto.request.RegisterRequest;
import com.prestape.dto.response.LoginResponse;
import com.prestape.dto.response.RegisterResponse;
import com.prestape.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService _authService;

    public AuthController(AuthService authService) {
        _authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Acceso correcto",_authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> login(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Usuario creado correctamente",_authService.register(request));
    }
}
