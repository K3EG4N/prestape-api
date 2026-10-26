package com.prestape.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.response.EquipmentCategoryListResponse;
import com.prestape.entity.EquipmentCategory;
import com.prestape.repository.EquipmentCategoryRepository;
import com.prestape.repository.EquipmentRepository;
import com.prestape.specifications.EquipmentCategorySpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentCategoryService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id"));

    private final EquipmentCategoryRepository categoryRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentCategoryService(
            EquipmentCategoryRepository categoryRepository,
            EquipmentRepository equipmentRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipmentCategoryListResponse> list(PaginationRequest pagination) {
        Specification<EquipmentCategory> spec = pagination.search() == null
                ? Specification.unrestricted()
                : EquipmentCategorySpecifications.matchesSearch(pagination.search());

        Page<EquipmentCategory> categories = categoryRepository.findAll(spec, pagination.toPageable(DEFAULT_SORT));

        Map<Long, Long> countByCategoryId = countEquipmentByCategory(categories.map(EquipmentCategory::getId).toList());

        return PageResponse.from(categories.map(c ->
                EquipmentCategoryListResponse.from(c, countByCategoryId.getOrDefault(c.getId(), 0L))));
    }

    private Map<Long, Long> countEquipmentByCategory(List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return equipmentRepository.countByCategoryIds(categoryIds).stream()
                .collect(Collectors.toMap(
                        EquipmentRepository.CategoryCount::getCategoryId,
                        EquipmentRepository.CategoryCount::getTotal));
    }
}
