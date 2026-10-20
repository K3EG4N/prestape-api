package com.prestape.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.response.MenuResponse;
import com.prestape.service.MenuService;

@RestController
@RequestMapping("/api/menus")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenus(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(menuService.getMenusByUserId(user.getId()));
    }
}
