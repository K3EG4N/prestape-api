package com.prestape.dto.request.kit.list;

import java.util.List;

import com.prestape.entity.enums.KitAvailability;

public record KitFilterRequest(
        List<KitAvailability> availabilities
) {
    public KitFilterRequest {
        availabilities = availabilities == null ? List.of() : availabilities;
    }
}
