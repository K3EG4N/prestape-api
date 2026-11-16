package com.prestape.specifications;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.dto.types.LoanStatusFilter;
import com.prestape.entity.Loan;
import com.prestape.entity.Person;
import com.prestape.entity.enums.LoanStatus;
import com.prestape.util.SearchUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class LoanSpecifications {

    private LoanSpecifications() {
    }

    public static Specification<Loan> requestedBy(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("requester").get("id"), userId);
    }

    public static Specification<Loan> hasAnyStatus(Collection<LoanStatusFilter> statuses) {
        return (root, query, cb) -> {
            Instant now = Instant.now();
            Expression<Instant> scheduledReturnAt = root.get("scheduledReturnAt");
            Expression<Instant> returnedAt = root.get("returnedAt");
            Predicate active = cb.equal(root.get("status"), LoanStatus.ACTIVE);
            Predicate returned = cb.equal(root.get("status"), LoanStatus.RETURNED);

            Predicate[] predicates = statuses.stream()
                    .map(status -> switch (status) {
                        case PENDING_PICKUP -> cb.equal(root.get("status"), LoanStatus.PENDING_PICKUP);
                        case ACTIVE -> cb.and(active, cb.greaterThanOrEqualTo(scheduledReturnAt, now));
                        case OVERDUE -> cb.and(active, cb.lessThan(scheduledReturnAt, now));
                        case RETURNED -> cb.and(returned, cb.lessThanOrEqualTo(returnedAt, scheduledReturnAt));
                        case RETURNED_LATE -> cb.and(returned, cb.greaterThan(returnedAt, scheduledReturnAt));
                        case CANCELLED -> cb.equal(root.get("status"), LoanStatus.CANCELLED);
                    })
                    .toArray(Predicate[]::new);

            return cb.or(predicates);
        };
    }

    public static Specification<Loan> scheduledPickupFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledPickupAt"), from);
    }

    public static Specification<Loan> scheduledPickupBefore(Instant to) {
        return (root, query, cb) -> cb.lessThan(root.get("scheduledPickupAt"), to);
    }

    public static Specification<Loan> scheduledReturnFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("scheduledReturnAt"), from);
    }

    public static Specification<Loan> scheduledReturnBefore(Instant to) {
        return (root, query, cb) -> cb.lessThan(root.get("scheduledReturnAt"), to);
    }

    public static Specification<Loan> matchesSearch(String search) {
        return (root, query, cb) -> cb.and(Arrays.stream(SearchUtils.toTerms(search))
                .map(term -> matchesTerm(root, query, cb, SearchUtils.toLikePattern(term)))
                .toArray(Predicate[]::new));
    }

    private static Predicate matchesTerm(Root<Loan> root, CriteriaQuery<?> query,
                                         CriteriaBuilder cb, String pattern) {
        Subquery<Long> requesters = query.subquery(Long.class);
        Root<Person> p = requesters.from(Person.class);
        requesters.select(p.get("id")).where(
                cb.equal(p.get("user"), root.get("requester")),
                cb.isNull(p.get("deletedAt")),
                cb.or(
                        SearchUtils.likeIgnoringAccents(cb, p.get("firstName"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("middleName"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("paternalSurname"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("maternalSurname"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("code"), pattern)));

        return cb.or(
                SearchUtils.likeIgnoringAccents(cb, root.get("code"), pattern),
                cb.exists(requesters));
    }
}
