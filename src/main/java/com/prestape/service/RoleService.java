package com.prestape.service;

import java.util.Comparator;
import java.util.List;

import com.prestape.dto.common.SelectOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prestape.dto.response.RoleResponse;
import com.prestape.repository.RoleRepository;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .sorted(Comparator.comparing(RoleResponse::externalName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SelectOption> options() {
        return roleRepository.findAll()
                .stream()
                .map(role -> new SelectOption(role.getId(), role.getName(), role.getExternalName()))
                .sorted(Comparator.comparing(SelectOption::name))
                .toList();
    }
}
