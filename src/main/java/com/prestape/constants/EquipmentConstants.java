package com.prestape.constants;

import org.springframework.data.domain.Sort;

public final class EquipmentConstants {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    public static final Sort OPTIONS_SORT = Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));
    public static final String CODE_PREFIX = "EQ-";
    public static final int CODE_LENGTH = 4;

    private EquipmentConstants() {
    }
}
