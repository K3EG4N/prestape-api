package com.prestape.specifications;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.entity.Equipment;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.util.SearchUtils;

import jakarta.persistence.criteria.Predicate;

public final class EquipmentSpecifications {

    private EquipmentSpecifications() {
    }

    public static Specification<Equipment> matchesSearch(String search) {
        return (root, query, cb) -> cb.and(Arrays.stream(SearchUtils.toTerms(search))
                .map(SearchUtils::toLikePattern)
                .map(pattern -> cb.or(
                        SearchUtils.likeIgnoringAccents(cb, root.get("name"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, root.get("code"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, root.get("serialNumber"), pattern)))
                .toArray(Predicate[]::new));
    }

    public static Specification<Equipment> hasAnyCategory(Collection<Long> categoryIds) {
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Equipment> hasAnyAvailability(Collection<EquipmentAvailability> availabilities) {
        return (root, query, cb) -> root.get("availability").in(availabilities);
    }

    public static Specification<Equipment> hasAnyCondition(Collection<EquipmentCondition> conditions) {
        return (root, query, cb) -> root.get("condition").in(conditions);
    }
}
