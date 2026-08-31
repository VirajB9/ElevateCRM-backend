package com.viraj.dmabackend.common.util;

import java.security.SecureRandom;

public final class PasswordGenerator {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";

    private static final int PASSWORD_LENGTH = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
        // Prevent instantiation
    }

    public static String generatePassword() {

        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(
                    CHARACTERS.charAt(
                            RANDOM.nextInt(CHARACTERS.length())
                    )
            );
        }

        return password.toString();
    }
}
