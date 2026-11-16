package com.prestape.dto.response;

import com.prestape.entity.Equipment;
import com.prestape.entity.enums.EquipmentCondition;

public record LoanEquipmentOption(
        Long id,
        String code,
        String name,
        EquipmentCondition condition
) {
    public static LoanEquipmentOption from(Equipment equipment) {
        return new LoanEquipmentOption(
                equipment.getId(),
                equipment.getCode(),
                equipment.getName(),
                equipment.getCondition());
    }
}
