package com.prestape.dto.response;

import java.util.Comparator;
import java.util.List;

import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.entity.UserStatus;

public record MeResponse(
        Long userId,
        String email,
        UserStatus status,
        boolean profileCompleted,
        List<RoleInfo> roles,
        PersonInfo person
) {
    public record RoleInfo(String name, String externalName) {
        static RoleInfo from(Role role) {
            return new RoleInfo(role.getName(), role.getExternalName());
        }
    }

    public record PersonInfo(String code, String displayName, String documentNumber) {
        static PersonInfo from(Person person) {
            if (person == null) return null;
            return new PersonInfo(person.getCode(), person.getDisplayName(), person.getDocumentNumber());
        }
    }

    public static MeResponse from(User user, Person person) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.isProfileCompleted(),
                user.getRoles().stream()
                        .map(RoleInfo::from)
                        .sorted(Comparator.comparing(RoleInfo::externalName))
                        .toList(),
                PersonInfo.from(person));
    }
}
