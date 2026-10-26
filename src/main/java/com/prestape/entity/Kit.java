package com.prestape.entity;

import java.util.HashSet;
import java.util.Set;

import com.prestape.entity.enums.KitAvailability;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "kits")
@SQLDelete(sql = "UPDATE kits SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Kit extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 7)
    private String code;

    private String description;

    @BatchSize(size = 50)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "kit_items",
            joinColumns = @JoinColumn(name = "kit_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<Equipment> equipments = new HashSet<>();

    public KitAvailability getAvailability() {
        if (equipments.isEmpty()) {
            return KitAvailability.EMPTY;
        }
        return equipments.stream().allMatch(Equipment::isLendable)
                ? KitAvailability.AVAILABLE
                : KitAvailability.UNAVAILABLE;
    }
}
