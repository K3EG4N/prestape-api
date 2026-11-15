package com.prestape.entity.enums;

import lombok.Getter;

@Getter
public enum EquipmentAvailability {
    AVAILABLE("Disponible"),
    LOANED("Prestado"),
    IN_MAINTENANCE("En mantenimiento"),
    RETIRED("De baja");

    private final String label;

    EquipmentAvailability(String label) {
        this.label = label;
    }

}
