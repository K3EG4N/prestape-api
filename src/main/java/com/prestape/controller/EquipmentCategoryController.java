package com.prestape.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.request.category.update.UpdateCategoryRequest;
import com.prestape.dto.response.EquipmentCategoryListResponse;
import com.prestape.service.EquipmentCategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipment-categories")
public class EquipmentCategoryController {

    private final EquipmentCategoryService categoryService;

    public EquipmentCategoryController(EquipmentCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<EquipmentCategoryListResponse>> list(
            @Valid @ModelAttribute PaginationRequest pagination) {
        return ApiResponse.ok(categoryService.list(pagination));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.update(id, request);
        return ApiResponse.success("Categoría actualizada correctamente");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success("Categoría eliminada correctamente");
    }
}
