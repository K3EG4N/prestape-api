package com.prestape.entity;

import static com.prestape.support.TestFixtures.loan;
import static com.prestape.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.prestape.entity.enums.LoanStatus;

@DisplayName("Préstamo")
class LoanTest {

    private static final Duration TOLERANCE = Duration.ofMinutes(45);

    @Test
    @DisplayName("La devolución justo en el límite de la tolerancia no es tardía")
    void returnAtToleranceLimitIsOnTime() {
        Loan loan = loan(1L, user(1L), LoanStatus.ACTIVE);
        Instant scheduled = Instant.parse("2026-11-16T15:00:00Z");
        loan.setScheduledReturnAt(scheduled);

        assertThat(loan.isPastDue(scheduled.plus(TOLERANCE), TOLERANCE)).isFalse();
        assertThat(loan.isPastDue(scheduled.plus(TOLERANCE).plusSeconds(1), TOLERANCE)).isTrue();
    }
}
