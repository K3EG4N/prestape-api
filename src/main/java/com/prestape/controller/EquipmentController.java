package com.prestape.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.prestape.dto.common.ApiResponse;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.request.equipment.create.CreateEquipmentRequest;
import com.prestape.dto.request.equipment.list.EquipmentFilterRequest;
import com.prestape.dto.request.equipment.update.UpdateEquipmentRequest;
import com.prestape.dto.response.EquipmentListResponse;
import com.prestape.service.EquipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipments")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ApiResponse<PageResponse<EquipmentListResponse>> list(
            @Valid @ModelAttribute PaginationRequest pagination,
            @Valid @ModelAttribute EquipmentFilterRequest filter) {
        return ApiResponse.ok(equipmentService.list(pagination, filter));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> create(@Valid @RequestBody CreateEquipmentRequest request) {
        equipmentService.create(request);
        return ApiResponse.success("Equipo creado correctamente");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateEquipmentRequest request) {
        equipmentService.update(id, request);
        return ApiResponse.success("Equipo actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ApiResponse.success("Equipo eliminado correctamente");
    }
}
