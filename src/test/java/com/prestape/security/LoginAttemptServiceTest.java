package com.prestape.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Seguridad: bloqueo por intentos fallidos")
class LoginAttemptServiceTest {

    private static final String EMAIL = "alumno@prestape.pe";

    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-11-16T15:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    @DisplayName("Bloquea la cuenta tras 5 intentos fallidos y la libera a los 15 minutos")
    void blocksAfterFiveFailedAttempts() {
        MovableClock clock = new MovableClock();
        LoginAttemptService service = new LoginAttemptService(clock);

        for (int attempt = 0; attempt < 4; attempt++) {
            service.loginFailed(EMAIL);
        }
        assertThat(service.isBlocked(EMAIL)).isFalse();

        service.loginFailed("  ALUMNO@prestape.pe ");
        assertThat(service.isBlocked(EMAIL)).isTrue();

        clock.advance(Duration.ofMinutes(16));
        assertThat(service.isBlocked(EMAIL)).isFalse();
    }
}
