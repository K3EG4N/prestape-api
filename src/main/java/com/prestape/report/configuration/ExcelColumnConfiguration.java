package com.prestape.report.configuration;

import java.util.function.Function;

public record ExcelColumnConfiguration<T>(String header, Function<T, Object> value) {
}
