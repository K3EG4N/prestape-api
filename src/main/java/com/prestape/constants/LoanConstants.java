package com.prestape.constants;

import java.util.Set;

import com.prestape.entity.enums.LoanStatus;
import org.springframework.data.domain.Sort;

public final class LoanConstants {

    public static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    public static final String CODE_PREFIX = "PR-";
    public static final int CODE_LENGTH = 4;
    public static final int MAX_EQUIPMENTS = 10;
    public static final Set<LoanStatus> OPEN_STATUSES = Set.of(LoanStatus.PENDING_PICKUP, LoanStatus.ACTIVE);
    public static final long AUTO_CANCEL_INTERVAL_MS = 300_000;
    public static final int OPTIONS_LIMIT = 30;
    public static final Sort OPTIONS_SORT = Sort.by("name", "code");

    private LoanConstants() {
    }
}
