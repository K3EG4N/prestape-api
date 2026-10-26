package com.prestape.specifications;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.dto.types.UserStatusFilter;
import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.entity.enums.UserStatus;
import com.prestape.util.SearchUtils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> matchesSearch(String search) {
        return (root, query, cb) -> cb.and(Arrays.stream(SearchUtils.toTerms(search))
                .map(term -> matchesTerm(root, query, cb, SearchUtils.toLikePattern(term)))
                .toArray(Predicate[]::new));
    }

    private static Predicate matchesTerm(Root<User> root, CriteriaQuery<?> query,
                                         CriteriaBuilder cb, String pattern) {
        Subquery<Long> people = query.subquery(Long.class);
        Root<Person> p = people.from(Person.class);
        people.select(p.get("id")).where(
                cb.equal(p.get("user"), root),
                cb.isNull(p.get("deletedAt")),
                cb.or(
                        SearchUtils.likeIgnoringAccents(cb, p.get("firstName"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("middleName"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("paternalSurname"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("maternalSurname"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("documentNumber"), pattern),
                        SearchUtils.likeIgnoringAccents(cb, p.get("code"), pattern)));

        return cb.or(
                SearchUtils.likeIgnoringAccents(cb, root.get("email"), pattern),
                SearchUtils.likeIgnoringAccents(cb, root.get("username"), pattern),
                cb.exists(people));
    }

    public static Specification<User> hasAnyRole(Collection<String> roleNames) {
        return (root, query, cb) -> {
            Subquery<Long> withRole = query.subquery(Long.class);
            Root<User> u = withRole.from(User.class);
            Join<User, Role> r = u.join("roles");
            withRole.select(u.get("id")).where(
                    cb.equal(u, root),
                    r.get("name").in(roleNames));

            return cb.exists(withRole);
        };
    }

    public static Specification<User> createdFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<User> createdBefore(Instant to) {
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), to);
    }

    public static Specification<User> hasAnyStatus(Collection<UserStatusFilter> statuses) {
        return (root, query, cb) -> {
            Predicate active = cb.equal(root.get("status"), UserStatus.ACTIVE);

            Predicate[] predicates = statuses.stream()
                    .map(status -> switch (status) {
                        case ACTIVE -> cb.and(active, cb.isTrue(root.get("profileCompleted")));
                        case INCOMPLETE -> cb.and(active, cb.isFalse(root.get("profileCompleted")));
                        case SUSPENDED -> cb.equal(root.get("status"), UserStatus.SUSPENDED);
                    })
                    .toArray(Predicate[]::new);

            return cb.or(predicates);
        };
    }
}
