package com.prestape.dto.request.loan.item;

import com.prestape.entity.enums.EquipmentCondition;
import jakarta.validation.constraints.NotNull;

public record LoanItemConditionRequest(
        @NotNull(message = "El equipo es obligatorio")
        Long equipmentId,

        @NotNull(message = "El estado del equipo es obligatorio")
        EquipmentCondition condition
) {
}
