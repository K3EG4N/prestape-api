package com.prestape.controller;

import java.util.List;

import com.prestape.dto.common.SelectOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.response.RoleResponse;
import com.prestape.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.ok(roleService.list());
    }

    @GetMapping("options")
    public ApiResponse<List<SelectOption>> options() {
        return ApiResponse.ok(roleService.options());
    }
}
