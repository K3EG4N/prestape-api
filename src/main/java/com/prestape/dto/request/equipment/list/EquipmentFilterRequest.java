package com.prestape.dto.request.equipment.list;

import java.util.List;

import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;

public record EquipmentFilterRequest(
        List<Long> categoryIds,

        List<EquipmentAvailability> availabilities,

        List<EquipmentCondition> conditions
) {
    public EquipmentFilterRequest {
        categoryIds = categoryIds == null ? List.of() : categoryIds;
        availabilities = availabilities == null ? List.of() : availabilities;
        conditions = conditions == null ? List.of() : conditions;
    }
}
