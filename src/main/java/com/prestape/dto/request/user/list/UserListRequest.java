package com.prestape.dto.request.user.list;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UserListRequest(
        @Min(value = 1, message = "La página debe ser mayor o igual a 1")
        Integer page,

        @Min(value = 1, message = "El tamaño de página debe ser mayor o igual a 1")
        @Max(value = 100, message = "El tamaño de página no puede ser mayor a 100")
        Integer size,

        String search,

        List<String> roles,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdTo
) {
    public UserListRequest {
        page = page == null ? 1 : page;
        size = size == null ? 15 : size;
        roles = roles == null ? List.of() : roles;
    }

    @AssertTrue(message = "La fecha inicial no puede ser mayor a la fecha final")
    public boolean isCreatedRangeValid() {
        return createdFrom == null || createdTo == null || !createdFrom.isAfter(createdTo);
    }
}
