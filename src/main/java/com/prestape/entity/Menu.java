package com.prestape.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity{
    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public int position;
}
