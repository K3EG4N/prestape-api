package com.prestape.dto.request.settings;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateSettingsRequest(
        @NotNull(message = "La tolerancia es obligatoria")
        @Min(value = 0, message = "La tolerancia no puede ser negativa")
        @Max(value = 240, message = "La tolerancia no puede superar los 240 minutos")
        Integer toleranceMinutes,

        @NotNull(message = "La duración máxima es obligatoria")
        @Min(value = 1, message = "La duración máxima debe ser al menos 1 día")
        @Max(value = 30, message = "La duración máxima no puede superar los 30 días")
        Integer maxDurationDays,

        @NotNull(message = "La anticipación mínima es obligatoria")
        @Min(value = 0, message = "La anticipación mínima no puede ser negativa")
        @Max(value = 72, message = "La anticipación mínima no puede superar las 72 horas")
        Integer minAdvanceHours,

        @NotNull(message = "El tiempo de cancelación automática es obligatorio")
        @Min(value = 1, message = "La cancelación automática debe ser al menos 1 hora")
        @Max(value = 72, message = "La cancelación automática no puede superar las 72 horas")
        Integer autoCancelHours,

        @NotNull(message = "Los límites por rol son obligatorios")
        List<@Valid @NotNull RoleLimitRequest> roleLimits,

        @NotNull(message = "El horario de atención es obligatorio")
        @Size(min = 7, max = 7, message = "El horario debe incluir los 7 días de la semana")
        List<@Valid @NotNull BusinessDayRequest> businessHours
) {
}
