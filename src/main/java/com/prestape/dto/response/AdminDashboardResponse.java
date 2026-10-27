package com.prestape.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.prestape.entity.Equipment;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.UserStatus;

public record AdminDashboardResponse(
        EquipmentSummary equipment,
        KitSummary kits,
        List<CategoryStock> categories,
        UserSummary users,
        List<AttentionEquipment> attention,
        Trends trends
) {
    public record Trends(Integer equipment, Integer kits, Integer users) {
    }

    public record EquipmentSummary(
            long total,
            Map<EquipmentAvailability, Long> byAvailability,
            Map<EquipmentCondition, Long> byCondition
    ) {
    }

    public record KitSummary(long total, long available) {
    }

    public record CategoryStock(Long id, String name, long total, long available, List<String> equipmentNames) {
    }

    public record UserSummary(
            long total,
            long active,
            long incomplete,
            long suspended,
            long newThisMonth,
            List<RecentUser> recentUsers
    ) {
    }

    public record RecentUser(
            Long id,
            String name,
            String email,
            String role,
            UserStatus status,
            boolean profileCompleted,
            Instant createdAt
    ) {
    }

    public record AttentionEquipment(
            Long id,
            String code,
            String name,
            String category,
            EquipmentAvailability availability,
            EquipmentCondition condition,
            String conditionNotes
    ) {
        public static AttentionEquipment from(Equipment equipment) {
            return new AttentionEquipment(
                    equipment.getId(),
                    equipment.getCode(),
                    equipment.getName(),
                    equipment.getCategory().getName(),
                    equipment.getAvailability(),
                    equipment.getCondition(),
                    equipment.getConditionNotes());
        }
    }
}
