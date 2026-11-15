package com.prestape.entity.enums;

import lombok.Getter;

@Getter
public enum KitAvailability {
    AVAILABLE("Disponible"),
    UNAVAILABLE("No disponible"),
    EMPTY("Sin equipos");

    private final String label;

    KitAvailability(String label) {
        this.label = label;
    }

}
