package com.prestape.constants;

import java.util.Set;

import com.prestape.security.Roles;

public final class SettingsConstants {

    public static final short SETTINGS_ID = 1;
    public static final int DAYS_IN_WEEK = 7;
    public static final int DEFAULT_MAX_EQUIPMENTS = 2;
    public static final int DEFAULT_MAX_ACTIVE_LOANS = 1;
    public static final Set<String> UNLIMITED_ROLES = Set.of(Roles.ADMIN, Roles.SUPERVISOR);

    private SettingsConstants() {
    }
}
