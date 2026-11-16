package com.prestape.entity;

import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "business_hours")
public class BusinessHour {
    @Id
    private Short dayOfWeek;

    @Column(name = "is_open", nullable = false)
    private boolean open;

    @Column(nullable = false)
    private LocalTime opensAt;

    @Column(nullable = false)
    private LocalTime closesAt;
}
