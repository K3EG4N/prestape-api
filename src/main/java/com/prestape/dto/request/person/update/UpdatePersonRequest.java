package com.prestape.dto.request.person.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePersonRequest(
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String documentNumber,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String firstName,

        @Size(max = 50, message = "El segundo nombre no puede superar los 50 caracteres")
        String middleName,

        @NotBlank(message = "El apellido paterno es obligatorio")
        @Size(max = 100, message = "El apellido paterno no puede superar los 100 caracteres")
        String paternalSurname,

        @Size(max = 50, message = "El apellido materno no puede superar los 50 caracteres")
        String maternalSurname,

        @NotBlank(message = "El celular es obligatorio")
        @Pattern(regexp = "\\d{9}", message = "El celular debe tener 9 dígitos")
        String phone
) {
}
