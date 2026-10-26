package com.prestape.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
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
}
