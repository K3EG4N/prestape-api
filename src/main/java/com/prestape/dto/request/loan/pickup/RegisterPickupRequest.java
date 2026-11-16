package com.prestape.dto.request.loan.pickup;

import java.util.List;

import com.prestape.dto.request.loan.item.LoanItemConditionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterPickupRequest(
        List<@Valid @NotNull LoanItemConditionRequest> items
) {
    public RegisterPickupRequest {
        items = items == null ? List.of() : items;
    }
}
