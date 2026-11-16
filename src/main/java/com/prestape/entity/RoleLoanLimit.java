package com.prestape.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role_loan_limits")
public class RoleLoanLimit {
    @Id
    private Long roleId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private int maxEquipments;

    @Column(nullable = false)
    private int maxActiveLoans;
}
