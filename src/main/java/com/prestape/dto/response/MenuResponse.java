package com.prestape.dto.response;

import com.prestape.entity.Menu;

public record MenuResponse(String name, String url, String icon) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(menu.getName(), menu.getUrl(), menu.getIcon());
    }
}
