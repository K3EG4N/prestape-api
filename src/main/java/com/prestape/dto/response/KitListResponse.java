package com.prestape.dto.response;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.prestape.entity.Equipment;
import com.prestape.entity.Kit;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.KitAvailability;

public record KitListResponse(
        Long id,
        String code,
        String name,
        String description,
        KitAvailability availability,
        List<ItemInfo> items,
        Instant createdAt
) {
    public record ItemInfo(
            Long id,
            String code,
            String name,
            EquipmentAvailability availability,
            EquipmentCondition condition
    ) {
        static ItemInfo from(Equipment equipment) {
            return new ItemInfo(
                    equipment.getId(),
                    equipment.getCode(),
                    equipment.getName(),
                    equipment.getAvailability(),
                    equipment.getCondition());
        }
    }

    public static KitListResponse from(Kit kit) {
        return new KitListResponse(
                kit.getId(),
                kit.getCode(),
                kit.getName(),
                kit.getDescription(),
                kit.getAvailability(),
                kit.getEquipments().stream()
                        .map(ItemInfo::from)
                        .sorted(Comparator.comparing(ItemInfo::name))
                        .toList(),
                kit.getCreatedAt());
    }
}
