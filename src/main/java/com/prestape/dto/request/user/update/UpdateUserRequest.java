package com.prestape.dto.request.user.update;

import com.prestape.dto.request.person.update.UpdatePersonRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
        String email,

        @NotNull(message = "Los datos personales son obligatorios")
        @Valid
        UpdatePersonRequest person
) {
}
