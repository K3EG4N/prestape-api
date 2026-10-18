package com.prestape.dto.response;

import com.prestape.entity.Role;

public record RoleResponse(Long id, String name, String externalName) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getExternalName());
    }
}
