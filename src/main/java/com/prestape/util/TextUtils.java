package com.prestape.util;

import org.springframework.util.StringUtils;

public final class TextUtils {

    private TextUtils() {
    }

    public static String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    public static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
