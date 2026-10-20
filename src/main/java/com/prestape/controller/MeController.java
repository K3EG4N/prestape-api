package com.prestape.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.request.person.create.CreatePersonRequest;
import com.prestape.dto.response.MeResponse;
import com.prestape.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(userService.getMe(user.getId()));
    }

    @PostMapping("/profile")
    public ApiResponse<Void> completeProfile(@AuthenticationPrincipal CustomUserDetails user,
                                             @Valid @RequestBody CreatePersonRequest request) {
        userService.completeProfile(user.getId(), request);
        return ApiResponse.success("Perfil completado correctamente");
    }
}
