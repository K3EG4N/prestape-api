package com.prestape.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
@SQLDelete(sql = "UPDATE roles SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Role extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    protected Role() { }

    public Role(String name) { this.name = name; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
}
