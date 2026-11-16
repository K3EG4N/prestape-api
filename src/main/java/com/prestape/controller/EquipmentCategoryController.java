package com.prestape.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.common.SelectOption;
import com.prestape.dto.request.category.update.UpdateCategoryRequest;
import com.prestape.dto.response.EquipmentCategoryListResponse;
import com.prestape.report.CategoryExcelReport;
import com.prestape.service.EquipmentCategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipment-categories")
public class EquipmentCategoryController {

    private final EquipmentCategoryService categoryService;
    private final CategoryExcelReport categoryExcelReport;

    public EquipmentCategoryController(
            EquipmentCategoryService categoryService,
            CategoryExcelReport categoryExcelReport
    ) {
        this.categoryService = categoryService;
        this.categoryExcelReport = categoryExcelReport;
    }

    @GetMapping
    public ApiResponse<PageResponse<EquipmentCategoryListResponse>> list(
            @Valid @ModelAttribute PaginationRequest pagination) {
        return ApiResponse.ok(categoryService.list(pagination));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String search) {
        return categoryExcelReport.generate(categoryService.listForExport(search)).toResponse();
    }

    @GetMapping("/options")
    public ApiResponse<List<SelectOption>> options() {
        return ApiResponse.ok(categoryService.options());
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
