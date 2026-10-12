package com.prestape.dto.response;

import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MeResponse(
        Long personId,
        String fullName,
        String email,
        String documentNumber,
        Long userId,
        List<String> roles
) {
    public static MeResponse from(Person person) {
        User user = person.getUser();

        return new MeResponse(
                person.getId(),
                buildFullName(person),
                user != null ? user.getEmail() : null,
                person.getDocumentNumber(),
                user != null ? user.getId() : null,
                user != null ? buildRoles(user) : null
        );
    }

    private static String buildFullName(Person p) {
        return Stream.of(p.getFirstName(), p.getMiddleName(), p.getPaternalSurname(), p.getMaternalSurname())
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private static List<String> buildRoles(User user) {
        return user.getRoles().stream()
                .map(Role::getExternalName)
                .sorted()
                .toList();
    }
}
