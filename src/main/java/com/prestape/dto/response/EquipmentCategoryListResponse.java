package com.prestape.dto.response;

import com.prestape.entity.EquipmentCategory;

public record EquipmentCategoryListResponse(
        Long id,
        String name,
        String description,
        long equipmentCount
) {
    public static EquipmentCategoryListResponse from(EquipmentCategory category, long equipmentCount) {
        return new EquipmentCategoryListResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                equipmentCount);
    }
}
