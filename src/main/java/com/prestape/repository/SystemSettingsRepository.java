package com.prestape.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prestape.entity.SystemSettings;

public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Short> {
}
