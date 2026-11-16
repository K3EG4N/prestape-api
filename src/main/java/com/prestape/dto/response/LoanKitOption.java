package com.prestape.dto.response;

import java.util.Comparator;
import java.util.List;

import com.prestape.entity.Kit;

public record LoanKitOption(
        Long id,
        String code,
        String name,
        List<LoanEquipmentOption> items
) {
    public static LoanKitOption from(Kit kit) {
        return new LoanKitOption(
                kit.getId(),
                kit.getCode(),
                kit.getName(),
                kit.getEquipments().stream()
                        .map(LoanEquipmentOption::from)
                        .sorted(Comparator.comparing(LoanEquipmentOption::name))
                        .toList());
    }
}
