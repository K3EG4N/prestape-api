package com.prestape.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.request.user.create.CreateUserRequest;
import com.prestape.dto.request.user.list.UserListRequest;
import com.prestape.dto.request.user.update.UpdateUserRequest;
import com.prestape.dto.response.UserListResponse;
import com.prestape.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserListResponse>> list(@Valid @ModelAttribute UserListRequest request) {
        return ApiResponse.ok(userService.list(request));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateUserRequest request) {
        userService.create(request);
        return ApiResponse.success("Usuario creado correctamente");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userService.update(id, request);
        return ApiResponse.success("Usuario actualizado correctamente");
    }
}
