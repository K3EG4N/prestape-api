package com.prestape.controller;

import com.prestape.dto.CustomUserDetails;
import com.prestape.dto.response.MeResponse;
import com.prestape.service.PersonService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal CustomUserDetails user) {
        return personService.getMe(user.getId());
    }
}
