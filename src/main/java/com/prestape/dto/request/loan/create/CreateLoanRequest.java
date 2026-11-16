package com.prestape.dto.request.loan.create;

import java.time.Instant;
import java.util.List;

import com.prestape.constants.LoanConstants;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLoanRequest(
        Long requesterId,

        @Size(max = LoanConstants.MAX_EQUIPMENTS, message = "Puede seleccionar como máximo 2 equipos")
        List<@NotNull Long> equipmentIds,

        Long kitId,

        @NotNull(message = "La fecha de recojo es obligatoria")
        @Future(message = "El recojo debe programarse en una fecha futura")
        Instant scheduledPickupAt,

        @NotNull(message = "La fecha de devolución es obligatoria")
        Instant scheduledReturnAt,

        @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres")
        String notes
) {
    public CreateLoanRequest {
        equipmentIds = equipmentIds == null ? List.of() : equipmentIds;
    }
}
