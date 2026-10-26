package com.prestape.util;

import java.text.Normalizer;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class SearchUtils {

    private static final String ACCENTED = "áéíóúüñàèìòù";
    private static final String PLAIN = "aeiouunaeiou";

    private SearchUtils() {
    }

    public static String[] toTerms(String search) {
        return removeAccents(search.toLowerCase()).trim().split("\\s+");
    }

    public static String toLikePattern(String term) {
        return "%" + escapeLike(term) + "%";
    }

    public static Predicate likeIgnoringAccents(CriteriaBuilder cb, Expression<String> field, String pattern) {
        Expression<String> plain = cb.function("translate", String.class,
                cb.lower(field), cb.literal(ACCENTED), cb.literal(PLAIN));
        return cb.like(plain, pattern, '\\');
    }

    private static String removeAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
