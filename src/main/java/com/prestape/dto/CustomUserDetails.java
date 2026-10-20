package com.prestape.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long id;
    private final boolean suspended;
    private final boolean profileCompleted;

    public CustomUserDetails(Long id, String email, String password,
                             Collection<? extends GrantedAuthority> authorities) {
        this(id, email, password, authorities, false, false);
    }

    public CustomUserDetails(Long id, String email, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean suspended, boolean profileCompleted) {
        super(email, password, authorities);
        this.id = id;
        this.suspended = suspended;
        this.profileCompleted = profileCompleted;
    }

    public Long getId() {
        return id;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }
}
