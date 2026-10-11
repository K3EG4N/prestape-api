package com.prestape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prestape.dto.UserDto;
import com.prestape.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::from)
                .toList();
    }
}
