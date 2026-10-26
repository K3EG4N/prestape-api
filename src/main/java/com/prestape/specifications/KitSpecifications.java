package com.prestape.specifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.entity.Equipment;
import com.prestape.entity.Kit;
import com.prestape.entity.enums.EquipmentAvailability;
import com.prestape.entity.enums.EquipmentCondition;
import com.prestape.entity.enums.KitAvailability;
import com.prestape.util.SearchUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class KitSpecifications {

    private KitSpecifications() {
    }

    public static Specification<Kit> matchesSearch(String search) {
        return (root, query, cb) -> cb.and(Arrays.stream(SearchUtils.toTerms(search))
                .map(SearchUtils::toLikePattern)
                .map(pattern -> cb.or(
                        SearchUtils.likeIgnoringAccents(cb, root.get("name"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, root.get("code"), pattern)))
                .toArray(Predicate[]::new));
    }

    public static Specification<Kit> hasAnyAvailability(Collection<KitAvailability> availabilities) {
        return (root, query, cb) -> cb.or(availabilities.stream()
                .map(availability -> switch (availability) {
                    case EMPTY -> cb.not(cb.exists(items(root, query, cb, false)));
                    case AVAILABLE -> cb.and(
                            cb.exists(items(root, query, cb, false)),
                            cb.not(cb.exists(items(root, query, cb, true))));
                    case UNAVAILABLE -> cb.exists(items(root, query, cb, true));
                })
                .toArray(Predicate[]::new));
    }

    private static Subquery<Long> items(Root<Kit> root, CriteriaQuery<?> query,
                                        CriteriaBuilder cb, boolean onlyUnlendable) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Kit> kit = subquery.from(Kit.class);
        Join<Kit, Equipment> equipment = kit.join("equipments");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(kit, root));
        predicates.add(cb.isNull(equipment.get("deletedAt")));
        if (onlyUnlendable) {
            predicates.add(cb.or(
                    cb.notEqual(equipment.get("availability"), EquipmentAvailability.AVAILABLE),
                    cb.equal(equipment.get("condition"), EquipmentCondition.DAMAGED)));
        }

        return subquery.select(equipment.get("id")).where(predicates.toArray(Predicate[]::new));
    }
}
