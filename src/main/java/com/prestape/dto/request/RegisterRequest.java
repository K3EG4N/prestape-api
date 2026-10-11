package com.prestape.dto.request;

import com.prestape.dto.types.LoginUserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotNull LoginUserType type,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String confirmPassword
) { }
