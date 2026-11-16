package com.prestape.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.request.settings.UpdateSettingsRequest;
import com.prestape.dto.response.SettingsResponse;
import com.prestape.service.SettingsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ApiResponse<SettingsResponse> get() {
        return ApiResponse.ok(settingsService.get());
    }

    @PutMapping
    public ApiResponse<Void> update(
            @Valid @RequestBody UpdateSettingsRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        settingsService.update(request, currentUser);
        return ApiResponse.success("Configuración actualizada correctamente");
    }
}
