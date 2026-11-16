package com.prestape.dto.loan;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.prestape.entity.BusinessHour;

public record LoanRules(
        Duration tolerance,
        Duration maxDuration,
        Duration minAdvance,
        Duration autoCancel,
        List<BusinessHour> businessHours,
        ZoneId zone
) {
    public boolean isWithinBusinessHours(Instant instant) {
        ZonedDateTime dateTime = instant.atZone(zone);
        BusinessHour day = businessHoursByDay().get((short) dateTime.getDayOfWeek().getValue());
        if (day == null || !day.isOpen()) {
            return false;
        }
        var time = dateTime.toLocalTime();
        return !time.isBefore(day.getOpensAt()) && !time.isAfter(day.getClosesAt());
    }

    private Map<Short, BusinessHour> businessHoursByDay() {
        return businessHours.stream()
                .collect(Collectors.toMap(BusinessHour::getDayOfWeek, Function.identity()));
    }
}
