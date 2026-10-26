package com.prestape.constants;

import org.springframework.data.domain.Sort;

public final class KitConstants {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    public static final String CODE_PREFIX = "KT-";
    public static final int CODE_LENGTH = 4;

    private KitConstants() {
    }
}
