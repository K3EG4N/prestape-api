package com.prestape.dto.request.settings;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoleLimitRequest(
        @NotNull(message = "El rol es obligatorio")
        Long roleId,

        @NotNull(message = "El máximo de equipos es obligatorio")
        @Min(value = 1, message = "El máximo de equipos debe ser al menos 1")
        @Max(value = 10, message = "El máximo de equipos no puede superar 10")
        Integer maxEquipments,

        @NotNull(message = "El máximo de préstamos activos es obligatorio")
        @Min(value = 1, message = "El máximo de préstamos activos debe ser al menos 1")
        @Max(value = 10, message = "El máximo de préstamos activos no puede superar 10")
        Integer maxActiveLoans
) {
}
