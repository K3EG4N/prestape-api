package com.prestape.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.prestape.constants.SecurityConstants;

@Service
public class LoginAttemptService {
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private final Map<String, Attempts> attemptsByEmail = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String email) {
        Attempts attempts = attemptsByEmail.get(key(email));
        if (attempts == null || attempts.lockedUntil() == null) {
            return false;
        }
        if (clock.instant().isAfter(attempts.lockedUntil())) {
            attemptsByEmail.remove(key(email));
            return false;
        }
        return true;
    }

    public void loginFailed(String email) {
        Attempts attempts = attemptsByEmail.merge(key(email), new Attempts(1, null),
                (current, ignored) -> current.increment());

        if (attempts.count() >= SecurityConstants.MAX_LOGIN_ATTEMPTS && attempts.lockedUntil() == null) {
            attemptsByEmail.put(key(email), attempts.lock(clock.instant().plus(SecurityConstants.LOGIN_LOCK_DURATION)));
            log.warn("Cuenta bloqueada temporalmente por intentos fallidos: email={}", mask(key(email)));
        }
    }

    static String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    public void loginSucceeded(String email) {
        attemptsByEmail.remove(key(email));
    }

    private static String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record Attempts(int count, Instant lockedUntil) {
        Attempts increment() {
            return new Attempts(count + 1, lockedUntil);
        }

        Attempts lock(Instant until) {
            return new Attempts(count, until);
        }
    }
}
