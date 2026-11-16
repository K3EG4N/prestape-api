package com.prestape.dto.request;

import com.prestape.constants.SecurityConstants;
import com.prestape.dto.types.LoginUserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull(message = "El tipo de usuario es obligatorio")
        LoginUserType type,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = SecurityConstants.MIN_PASSWORD_LENGTH, max = 72,
                message = "La contraseña debe tener entre " + SecurityConstants.MIN_PASSWORD_LENGTH + " y 72 caracteres")
        String password,

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        String confirmPassword
) { }
