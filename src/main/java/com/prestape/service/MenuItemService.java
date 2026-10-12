package com.prestape.service;

import com.prestape.dto.response.MenuByUserIdResponse;
import com.prestape.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public List<MenuByUserIdResponse> getMenuByUserId(Long userId) {
        return menuItemRepository.findVisibleByUserId(userId);
    }
}
