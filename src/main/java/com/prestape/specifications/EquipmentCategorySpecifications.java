package com.prestape.specifications;

import java.util.Arrays;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.entity.EquipmentCategory;
import com.prestape.util.SearchUtils;

import jakarta.persistence.criteria.Predicate;

public final class EquipmentCategorySpecifications {

    private EquipmentCategorySpecifications() {
    }

    public static Specification<EquipmentCategory> matchesSearch(String search) {
        return (root, query, cb) -> cb.and(Arrays.stream(SearchUtils.toTerms(search))
                .map(SearchUtils::toLikePattern)
                .map(pattern -> cb.or(
                        SearchUtils.likeIgnoringAccents(cb, root.get("name"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, root.get("description"), pattern)))
                .toArray(Predicate[]::new));
    }
}
