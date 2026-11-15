package com.viraj.dmabackend.common.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public final class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%&*";
    private static final String ALL_CHARACTERS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;

    private static final int PASSWORD_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
        // Prevent instantiation
    }

    public static String generatePassword() {

        List<Character> chars = new ArrayList<>(PASSWORD_LENGTH);

        // Deterministically include at least one from each required character category
        chars.add(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        chars.add(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        chars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // Fill remaining length from combined character pool
        for (int i = 4; i < PASSWORD_LENGTH; i++) {
            chars.add(ALL_CHARACTERS.charAt(RANDOM.nextInt(ALL_CHARACTERS.length())));
        }

        // Cryptographically secure shuffle using Fisher-Yates
        for (int i = chars.size() - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            Character temp = chars.get(i);
            chars.set(i, chars.get(j));
            chars.set(j, temp);
        }

        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (Character c : chars) {
            sb.append(c);
        }

        return sb.toString();
    }
}
