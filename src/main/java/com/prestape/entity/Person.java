package com.prestape.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Person(
            User user,
            String code,
            String documentNumber,
            String firstName,
            String middleName,
            String paternalSurname,
            String maternalSurname,
            String phone
    ) {
        this.user = user;
        this.code = code;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.phone = phone;
    }

    public void update(
            String documentNumber,
            String firstName,
            String middleName,
            String paternalSurname,
            String maternalSurname,
            String phone
    ) {
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.paternalSurname = paternalSurname;
        this.maternalSurname = maternalSurname;
        this.phone = phone;
    }

    public String getFullName() {
        return Stream.of(firstName, middleName, paternalSurname, maternalSurname)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}
