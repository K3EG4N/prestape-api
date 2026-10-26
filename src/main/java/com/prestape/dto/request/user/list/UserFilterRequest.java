package com.prestape.dto.request.user.list;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.prestape.dto.types.UserStatusFilter;

import jakarta.validation.constraints.AssertTrue;

public record UserFilterRequest(
        List<String> roles,

        List<UserStatusFilter> statuses,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant createdTo
) {
    public UserFilterRequest {
        roles = roles == null ? List.of() : roles;
        statuses = statuses == null ? List.of() : statuses;
    }

    @AssertTrue(message = "La fecha inicial no puede ser mayor a la fecha final")
    public boolean isCreatedRangeValid() {
        return createdFrom == null || createdTo == null || !createdFrom.isAfter(createdTo);
    }
}
