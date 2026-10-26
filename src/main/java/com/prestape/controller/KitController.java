package com.prestape.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.request.kit.create.CreateKitRequest;
import com.prestape.dto.request.kit.list.KitFilterRequest;
import com.prestape.dto.request.kit.update.UpdateKitRequest;
import com.prestape.dto.response.KitListResponse;
import com.prestape.service.KitService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kits")
public class KitController {

    private final KitService kitService;

    public KitController(KitService kitService) {
        this.kitService = kitService;
    }

    @GetMapping
    public ApiResponse<PageResponse<KitListResponse>> list(
            @Valid @ModelAttribute PaginationRequest pagination,
            @Valid @ModelAttribute KitFilterRequest filter) {
        return ApiResponse.ok(kitService.list(pagination, filter));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> create(@Valid @RequestBody CreateKitRequest request) {
        kitService.create(request);
        return ApiResponse.success("Kit creado correctamente");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateKitRequest request) {
        kitService.update(id, request);
        return ApiResponse.success("Kit actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        kitService.delete(id);
        return ApiResponse.success("Kit eliminado correctamente");
    }
}
