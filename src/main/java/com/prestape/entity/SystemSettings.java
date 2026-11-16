package com.prestape.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "system_settings")
public class SystemSettings {
    @Id
    private Short id;

    @Column(nullable = false)
    private int toleranceMinutes;

    @Column(nullable = false)
    private int maxDurationDays;

    @Column(nullable = false)
    private int minAdvanceHours;

    @Column(nullable = false)
    private int autoCancelHours;

    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    private User updatedBy;
}
