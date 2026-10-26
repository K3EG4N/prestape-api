package com.prestape.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.prestape.constants.CategoryConstants;
import com.prestape.dto.common.PageResponse;
import com.prestape.dto.common.PaginationRequest;
import com.prestape.dto.common.SelectOption;
import com.prestape.dto.request.category.update.UpdateCategoryRequest;
import com.prestape.dto.response.EquipmentCategoryListResponse;
import com.prestape.entity.EquipmentCategory;
import com.prestape.exception.BusinessException;
import com.prestape.exception.ConflictException;
import com.prestape.exception.ResourceNotFoundException;
import com.prestape.repository.EquipmentCategoryRepository;
import com.prestape.repository.EquipmentRepository;
import com.prestape.specifications.EquipmentCategorySpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EquipmentCategoryService {

    private static final Logger log = LoggerFactory.getLogger(EquipmentCategoryService.class);

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

        Page<EquipmentCategory> categories = categoryRepository.findAll(spec, pagination.toPageable(CategoryConstants.DEFAULT_SORT));

        Map<Long, Long> countByCategoryId = countEquipmentByCategory(categories.map(EquipmentCategory::getId).toList());

        return PageResponse.from(categories.map(c ->
                EquipmentCategoryListResponse.from(c, countByCategoryId.getOrDefault(c.getId(), 0L))));
    }

    @Transactional(readOnly = true)
    public List<SelectOption> options() {
        return categoryRepository.findAll(CategoryConstants.DEFAULT_SORT).stream()
                .map(category -> new SelectOption(category.getId(), category.getName(), category.getDescription()))
                .toList();
    }

    @Transactional
    public void update(Long id, UpdateCategoryRequest request) {
        EquipmentCategory category = findCategory(id);
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Ya existe una categoría con ese nombre");
        }

        category.setName(name);
        category.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        log.info("Categoría actualizada: categoryId={}", id);
    }

    @Transactional
    public void delete(Long id) {
        EquipmentCategory category = findCategory(id);

        long equipmentCount = equipmentRepository.countByCategoryId(id);
        if (equipmentCount > 0) {
            throw new BusinessException(
                    "No se puede eliminar la categoría porque tiene " + equipmentCount + " equipos asociados");
        }

        categoryRepository.delete(category);
        log.info("Categoría eliminada: categoryId={}", id);
    }

    private EquipmentCategory findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
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
