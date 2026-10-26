package com.prestape.entity;

import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "equipments")
@SQLDelete(sql = "UPDATE equipments SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Equipment extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 7)
    private String code;

    @Column(nullable = false, length = 50)
    private String serialNumber;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentAvailability availability = EquipmentAvailability.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_condition", nullable = false, length = 20)
    private EquipmentCondition condition = EquipmentCondition.GOOD;

    @Column(length = 255)
    private String conditionNotes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_category_id", nullable = false)
    private EquipmentCategory category;

    public boolean isLendable() {
        return availability == EquipmentAvailability.AVAILABLE
                && condition != EquipmentCondition.DAMAGED;
    }
}
