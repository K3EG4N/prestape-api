package com.prestape.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseEntity{
    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public int position;

    @Column(nullable = false)
    public String url;

    public String icon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;
}
