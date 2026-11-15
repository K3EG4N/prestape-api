package com.prestape.entity.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("Activo"),
    SUSPENDED("Suspendido");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

}
