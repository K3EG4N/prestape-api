package com.prestape.entity.enums;

import lombok.Getter;

@Getter
public enum LoanStatus {
    PENDING_PICKUP("Pendiente de recojo"),
    ACTIVE("En curso"),
    RETURNED("Devuelto"),
    CANCELLED("Cancelado");

    private final String label;

    LoanStatus(String label) {
        this.label = label;
    }

}
