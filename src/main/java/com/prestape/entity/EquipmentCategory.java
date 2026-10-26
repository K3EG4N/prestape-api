package com.prestape.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@Entity
@Table(name = "equipment_categories")
@SQLDelete(sql = "UPDATE equipment_categories SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class EquipmentCategory extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;

    private String description;
}
