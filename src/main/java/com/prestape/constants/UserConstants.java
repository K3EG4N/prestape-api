package com.prestape.constants;

import org.springframework.data.domain.Sort;

public final class UserConstants {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private UserConstants() {
    }
}
