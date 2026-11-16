package com.prestape.dto.request.loan.returns;

import java.util.List;

import com.prestape.dto.request.loan.item.LoanItemConditionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record RegisterReturnRequest(
        List<@Valid @NotNull LoanItemConditionRequest> items
) {
    public RegisterReturnRequest {
        items = items == null ? List.of() : items;
    }
}
