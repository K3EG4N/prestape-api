package com.prestape.dto.response;

import com.prestape.entity.Person;

public record PersonSummaryResponse(
        String code,
        String fullName,
        String firstName,
        String middleName,
        String paternalSurname,
        String maternalSurname,
        String documentNumber,
        String phone
) {
    public static PersonSummaryResponse from(Person person) {
        if (person == null) return null;

        return new PersonSummaryResponse(
                person.getCode(),
                person.getFullName(),
                person.getFirstName(),
                person.getMiddleName(),
                person.getPaternalSurname(),
                person.getMaternalSurname(),
                person.getDocumentNumber(),
                person.getPhone());
    }
}
