package com.prestape.dto.response;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.prestape.entity.Person;
import com.prestape.entity.User;

public record UserListResponse(
        Long id,
        String email,
        List<RoleResponse> roles,
        boolean profileCompleted,
        PersonSummaryResponse person,
        Instant createdAt
) {
    public static UserListResponse from(User user, Person person) {
        return new UserListResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(RoleResponse::from)
                        .sorted(Comparator.comparing(RoleResponse::externalName))
                        .toList(),
                user.isProfileCompleted(),
                PersonSummaryResponse.from(person),
                user.getCreatedAt());
    }
}
