package com.prestape.report.configuration;

import java.util.function.Function;

public record ExcelColumnConfiguration<T>(String header, Function<T, Object> value, String format) {

    public static final String DATE_TIME_FORMAT = "dd/mm/yyyy hh:mm";

    public ExcelColumnConfiguration(String header, Function<T, Object> value) {
        this(header, value, null);
    }

    public static <T> ExcelColumnConfiguration<T> dateTime(String header, Function<T, Object> value) {
        return new ExcelColumnConfiguration<>(header, value, DATE_TIME_FORMAT);
    }
}
