package com.prestape.service;

import com.prestape.dto.response.MeResponse;
import com.prestape.exception.ResourceNotFoundException;
import com.prestape.repository.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class PersonService {
    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public MeResponse getMe(Long userId) {
        return personRepository.findByUserId(userId)
                .map(MeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));
    }
}
