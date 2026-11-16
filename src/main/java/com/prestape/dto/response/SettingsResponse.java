package com.prestape.dto.response;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.prestape.entity.BusinessHour;

public record SettingsResponse(
        int toleranceMinutes,
        int maxDurationDays,
        int minAdvanceHours,
        int autoCancelHours,
        List<RoleLimitInfo> roleLimits,
        List<BusinessDayInfo> businessHours,
        Instant updatedAt,
        String updatedBy
) {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public record RoleLimitInfo(
            Long roleId, String roleName, String roleLabel, int maxEquipments, int maxActiveLoans) {
    }

    public record BusinessDayInfo(short day, boolean open, String opensAt, String closesAt) {
        public static BusinessDayInfo from(BusinessHour hour) {
            return new BusinessDayInfo(
                    hour.getDayOfWeek(),
                    hour.isOpen(),
                    hour.getOpensAt().format(TIME_FORMAT),
                    hour.getClosesAt().format(TIME_FORMAT));
        }
    }
}
