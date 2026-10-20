package com.prestape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prestape.dto.response.MenuResponse;
import com.prestape.repository.MenuRepository;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusByUserId(Long userId) {
        return menuRepository.findVisibleByUserId(userId).stream()
                .map(MenuResponse::from)
                .toList();
    }
}
