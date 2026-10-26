package com.prestape.dto.request.equipment.create;

import com.prestape.entity.enums.EquipmentCondition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEquipmentRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotBlank(message = "La serie es obligatoria")
        @Size(max = 50, message = "La serie no puede superar los 50 caracteres")
        String serialNumber,

        @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
        String description,

        @NotNull(message = "Debe seleccionar una categoría")
        Long categoryId,

        @NotNull(message = "Debe seleccionar la condición del equipo")
        EquipmentCondition condition,

        @Size(max = 255, message = "Las observaciones no pueden superar los 255 caracteres")
        String conditionNotes
) {
}
