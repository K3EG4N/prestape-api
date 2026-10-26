package com.prestape.service;

import com.prestape.constants.PersonConstants;
import com.prestape.dto.request.person.create.CreatePersonRequest;
import com.prestape.dto.request.person.update.UpdatePersonRequest;
import com.prestape.entity.Person;
import com.prestape.entity.User;
import com.prestape.exception.ConflictException;
import com.prestape.repository.PersonRepository;
import com.prestape.util.RandomCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Transactional
    public void createForUser(User user, CreatePersonRequest request) {
        if (user.isProfileCompleted()) {
            throw new ConflictException("El usuario ya tiene un perfil registrado");
        }

        if (personRepository.existsByDocumentNumberAndDeletedAtIsNull(request.documentNumber())) {
            throw new ConflictException("El DNI ya está registrado");
        }

        Person person = new Person(
                user,
                RandomCodeGenerator.generateUnique(
                        PersonConstants.CODE_PREFIX, PersonConstants.CODE_LENGTH, personRepository::existsByCode),
                request.documentNumber(),
                request.firstName().trim(),
                trimToNull(request.middleName()),
                request.paternalSurname().trim(),
                trimToNull(request.maternalSurname()),
                request.phone());

        Person saved = personRepository.save(person);
        user.markProfileCompleted();
    }

    @Transactional(readOnly = true)
    public Optional<Person> findActiveByUserId(Long userId) {
        return personRepository.findByUserIdAndDeletedAtIsNull(userId);
    }

    @Transactional
    public void update(Person person, UpdatePersonRequest request) {
        if (personRepository.existsByDocumentNumberAndDeletedAtIsNullAndIdNot(
                request.documentNumber(), person.getId())) {
            throw new ConflictException("El DNI ya está registrado");
        }

        person.update(
                request.documentNumber(),
                request.firstName().trim(),
                trimToNull(request.middleName()),
                request.paternalSurname().trim(),
                trimToNull(request.maternalSurname()),
                request.phone());
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
