package com.prestape.specifications;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;

import com.prestape.entity.Person;
import com.prestape.entity.Role;
import com.prestape.entity.User;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> matchesSearch(String search) {
        return (root, query, cb) -> {
            String pattern = "%" + escapeLike(search.toLowerCase()) + "%";

            Subquery<Long> people = query.subquery(Long.class);
            Root<Person> p = people.from(Person.class);
            Expression<String> fullName = cb.concat(
                    cb.concat(cb.concat(p.<String>get("firstName"), " "), p.<String>get("paternalSurname")),
                    cb.concat(" ", cb.coalesce(p.<String>get("maternalSurname"), "")));
            people.select(p.get("id")).where(
                    cb.equal(p.get("user"), root),
                    cb.isNull(p.get("deletedAt")),
                    cb.or(
                            cb.like(cb.lower(fullName), pattern, '\\'),
                            cb.like(p.get("documentNumber"), pattern, '\\'),
                            cb.like(cb.lower(p.get("code")), pattern, '\\')));

            return cb.or(
                    cb.like(cb.lower(root.get("email")), pattern, '\\'),
                    cb.like(cb.lower(root.get("username")), pattern, '\\'),
                    cb.exists(people));
        };
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

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
