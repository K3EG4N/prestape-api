package com.prestape.constants;

import org.springframework.data.domain.Sort;

public final class CategoryConstants {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));

    private CategoryConstants() {
    }
}
