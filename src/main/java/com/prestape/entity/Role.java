package com.prestape.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "roles")
@SQLDelete(sql = "UPDATE roles SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Role extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false)
    private String externalName;

    protected Role() { }

    public Role(String name) { this.name = name; }
}
