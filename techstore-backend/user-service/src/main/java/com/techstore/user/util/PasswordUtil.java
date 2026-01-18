package com.techstore.user.util;

import java.security.SecureRandom;

public final class PasswordUtil {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=";
    private static final String ALL = LOWER + UPPER + DIGIT + SPECIAL;

    private static final SecureRandom random = new SecureRandom();

    private PasswordUtil() {}

    public static String randomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }
        return sb.toString();
    }
}
