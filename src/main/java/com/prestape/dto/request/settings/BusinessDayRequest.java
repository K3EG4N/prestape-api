package com.prestape.dto.request.settings;

import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BusinessDayRequest(
        @NotNull(message = "El día es obligatorio")
        @Min(value = 1, message = "El día debe estar entre 1 y 7")
        @Max(value = 7, message = "El día debe estar entre 1 y 7")
        Short day,

        @NotNull(message = "Indique si el día está abierto")
        Boolean open,

        @NotNull(message = "La hora de apertura es obligatoria")
        LocalTime opensAt,

        @NotNull(message = "La hora de cierre es obligatoria")
        LocalTime closesAt
) {
    @AssertTrue(message = "La hora de cierre debe ser posterior a la de apertura")
    public boolean isRangeValid() {
        return opensAt == null || closesAt == null || closesAt.isAfter(opensAt);
    }
}
