package com.prestape.constants;

import java.time.Duration;

public final class SecurityConstants {

    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final Duration LOGIN_LOCK_DURATION = Duration.ofMinutes(15);
    public static final int MIN_PASSWORD_LENGTH = 8;

    private SecurityConstants() {
    }
}
