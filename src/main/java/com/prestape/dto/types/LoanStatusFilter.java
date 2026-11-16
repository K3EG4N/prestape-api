package com.prestape.dto.types;

import lombok.Getter;

@Getter
public enum LoanStatusFilter {
    PENDING_PICKUP("Pendiente de recojo"),
    ACTIVE("En curso"),
    OVERDUE("Vencido"),
    RETURNED("Devuelto"),
    RETURNED_LATE("Devuelto con retraso"),
    CANCELLED("Cancelado");

    private final String label;

    LoanStatusFilter(String label) {
        this.label = label;
    }
}
