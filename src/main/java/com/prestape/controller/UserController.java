package com.prestape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.request.user.create.CreateUserRequest;
import com.prestape.dto.request.user.list.UserListRequest;
import com.prestape.dto.request.user.update.UpdateUserRequest;
import com.prestape.dto.response.UserListResponse;
import com.prestape.report.UserExcelReport;
import com.prestape.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
public class UserController {

    private final UserService userService;
    private final UserExcelReport userExcelReport;

    public UserController(UserService userService, UserExcelReport userExcelReport) {
        this.userService = userService;
        this.userExcelReport = userExcelReport;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserListResponse>> list(@Valid @ModelAttribute UserListRequest request) {
        return ApiResponse.ok(userService.list(request));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@Valid @ModelAttribute UserListRequest request) {
        return userExcelReport.generate(userService.listForExport(request)).toResponse();
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateUserRequest request) {
        userService.create(request);
        return ApiResponse.success("Usuario creado correctamente");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userService.update(id, request);
        return ApiResponse.success("Usuario actualizado correctamente");
    }

    @PutMapping("/{id}/suspend")
    public ApiResponse<Void> suspend(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        userService.suspend(id, currentUser.getId());
        return ApiResponse.success("Usuario suspendido correctamente");
    }

    @PutMapping("/{id}/reactivate")
    public ApiResponse<Void> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ApiResponse.success("Usuario reactivado correctamente");
    }
}
