package com.prestape.specifications;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.dto.types.UserStatusFilter;
import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;
import com.prestape.entity.UserStatus;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class UserSpecifications {

    private static final String ACCENTED = "áéíóúüñàèìòù";
    private static final String PLAIN = "aeiouunaeiou";

    private UserSpecifications() {
    }

    public static Specification<User> matchesSearch(String search) {
        String[] terms = removeAccents(search.toLowerCase()).trim().split("\\s+");

        return (root, query, cb) -> cb.and(Arrays.stream(terms)
                .map(term -> matchesTerm(root, query, cb, "%" + escapeLike(term) + "%"))
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
                        likeIgnoringAccents(cb, p.get("firstName"), pattern),
                        likeIgnoringAccents(cb, p.get("middleName"), pattern),
                        likeIgnoringAccents(cb, p.get("paternalSurname"), pattern),
                        likeIgnoringAccents(cb, p.get("maternalSurname"), pattern),
                        likeIgnoringAccents(cb, p.get("documentNumber"), pattern),
                        likeIgnoringAccents(cb, p.get("code"), pattern)));

        return cb.or(
                likeIgnoringAccents(cb, root.get("email"), pattern),
                likeIgnoringAccents(cb, root.get("username"), pattern),
                cb.exists(people));
    }

    private static Predicate likeIgnoringAccents(CriteriaBuilder cb, Expression<String> field, String pattern) {
        Expression<String> plain = cb.function("translate", String.class,
                cb.lower(field), cb.literal(ACCENTED), cb.literal(PLAIN));
        return cb.like(plain, pattern, '\\');
    }

    private static String removeAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
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

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
