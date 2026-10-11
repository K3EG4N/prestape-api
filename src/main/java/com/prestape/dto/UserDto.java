package com.prestape.dto;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import com.prestape.entity.Role;
import com.prestape.entity.User;

public record UserDto(Long id, String username, String email, Set<String> roles, Instant createdAt) {

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt());
    }
}
