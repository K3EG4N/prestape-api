package com.prestape.util;

import java.security.SecureRandom;
import java.util.function.Predicate;

public final class RandomCodeGenerator {

    // Sin caracteres que se confunden al dictar o copiar: 0/O, 1/I/L
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 10;

    private RandomCodeGenerator() {
    }

    public static String generate(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    public static String generateUnique(String prefix, int length, Predicate<String> isTaken) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = prefix + generate(length);
            if (!isTaken.test(code)) {
                return code;
            }
        }
        throw new IllegalStateException("No se pudo generar un código único");
    }
}
