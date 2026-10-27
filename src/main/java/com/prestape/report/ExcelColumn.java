package com.prestape.report;

import java.util.function.Function;

public record ExcelColumn<T>(String header, Function<T, Object> value) {
}
