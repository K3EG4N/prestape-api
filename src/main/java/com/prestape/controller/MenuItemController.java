package com.prestape.controller;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.response.MenuByUserIdResponse;
import com.prestape.service.MenuItemService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu-item")
public class MenuItemController {
    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public ApiResponse<List<MenuByUserIdResponse>> getMenus(@AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(menuItemService.getMenuByUserId(user.getId()));
    }
}
