package com.prestape.entity.enums;

import lombok.Getter;

@Getter
public enum EquipmentCondition {
    GOOD("Bueno"),
    WORN("Desgastado"),
    DAMAGED("Dañado");

    private final String label;

    EquipmentCondition(String label) {
        this.label = label;
    }

}
