package com.prestape.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "people")
@SQLDelete(sql = "UPDATE people SET deleted_at = now() WHERE id = ?")
public class Person extends BaseEntity{
    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false, length = 8)
    private String documentNumber;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(nullable = false, length = 100)
    private String paternalSurname;

    @Column(length = 50)
    private String maternalSurname;

    @Column(nullable = false, length = 9)
    private String phone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
}
