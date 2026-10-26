package com.prestape.dto.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PaginationRequest(
        @Min(value = 1, message = "La página debe ser mayor o igual a 1")
        Integer page,

        @Min(value = 1, message = "El tamaño de página debe ser mayor o igual a 1")
        @Max(value = 100, message = "El tamaño de página no puede ser mayor a 100")
        Integer size,

        String search
) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 15;

    public PaginationRequest {
        page = page == null ? DEFAULT_PAGE : page;
        size = size == null ? DEFAULT_SIZE : size;
        search = StringUtils.hasText(search) ? search.trim() : null;
    }

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page - 1, size, sort);
    }
}
