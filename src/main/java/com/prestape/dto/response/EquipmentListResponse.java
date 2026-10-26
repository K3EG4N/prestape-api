package com.prestape.dto.response;

import java.time.Instant;

import com.prestape.entity.Equipment;
import com.prestape.entity.EquipmentCategory;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;

public record EquipmentListResponse(
        Long id,
        String code,
        String serialNumber,
        String name,
        String description,
        EquipmentAvailability availability,
        EquipmentCondition condition,
        String conditionNotes,
        CategoryInfo category,
        Instant createdAt
) {
    public record CategoryInfo(Long id, String name) {
        static CategoryInfo from(EquipmentCategory category) {
            return new CategoryInfo(category.getId(), category.getName());
        }
    }

    public static EquipmentListResponse from(Equipment equipment) {
        return new EquipmentListResponse(
                equipment.getId(),
                equipment.getCode(),
                equipment.getSerialNumber(),
                equipment.getName(),
                equipment.getDescription(),
                equipment.getAvailability(),
                equipment.getCondition(),
                equipment.getConditionNotes(),
                CategoryInfo.from(equipment.getCategory()),
                equipment.getCreatedAt());
    }
}
