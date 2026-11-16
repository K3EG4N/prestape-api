package com.prestape.dto.request.loan.list;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.prestape.dto.types.LoanStatusFilter;

import jakarta.validation.constraints.AssertTrue;

public record LoanFilterRequest(
        List<LoanStatusFilter> statuses,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant pickupFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant pickupTo,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant returnFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant returnTo
) {
    public LoanFilterRequest {
        statuses = statuses == null ? List.of() : statuses;
    }

    @AssertTrue(message = "La fecha inicial de recojo no puede ser mayor a la fecha final")
    public boolean isPickupRangeValid() {
        return pickupFrom == null || pickupTo == null || !pickupFrom.isAfter(pickupTo);
    }

    @AssertTrue(message = "La fecha inicial de devolución no puede ser mayor a la fecha final")
    public boolean isReturnRangeValid() {
        return returnFrom == null || returnTo == null || !returnFrom.isAfter(returnTo);
    }
}
