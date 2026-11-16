package com.prestape.report.configuration;

import java.util.List;
import java.util.function.Function;

public record ExcelGroupedSheetConfiguration<T, D>(
        List<ExcelColumnConfiguration<T>> leadingColumns,
        List<ExcelColumnConfiguration<D>> detailColumns,
        List<ExcelColumnConfiguration<T>> trailingColumns,
        Function<T, List<D>> details
) {
    public int totalColumns() {
        return leadingColumns.size() + detailColumns.size() + trailingColumns.size();
    }
}
