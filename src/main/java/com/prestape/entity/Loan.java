package com.prestape.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.prestape.entity.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "loans")
@SQLDelete(sql = "UPDATE loans SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Loan extends BaseEntity {
    @Column(nullable = false, length = 7)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kit_id")
    private Kit kit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoanStatus status = LoanStatus.PENDING_PICKUP;

    @Column(nullable = false)
    private Instant scheduledPickupAt;

    private Instant pickedUpAt;

    @Column(nullable = false)
    private Instant scheduledReturnAt;

    private Instant returnedAt;

    private Instant cancelledAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registered_by_id", nullable = false)
    private User registeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivered_by_id")
    private User deliveredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_id")
    private User receivedBy;

    @Column(length = 255)
    private String notes;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanItem> items = new ArrayList<>();

    public void addItem(LoanItem item) {
        item.setLoan(this);
        items.add(item);
    }

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && Instant.now().isAfter(scheduledReturnAt);
    }

    public boolean isReturnedLate() {
        return returnedAt != null && returnedAt.isAfter(scheduledReturnAt);
    }
}
