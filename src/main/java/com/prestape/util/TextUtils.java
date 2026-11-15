package com.prestape.util;

public final class TextUtils {

    private TextUtils() {
    }

    public static String capitalize(String value) {
        if (value == null || value.isBlank()) return "";
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
