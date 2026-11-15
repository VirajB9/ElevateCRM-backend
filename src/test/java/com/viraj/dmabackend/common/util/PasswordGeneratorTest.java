package com.viraj.dmabackend.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private static final Pattern ENTERPRISE_COMPLEXITY_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])\\S+$"
    );

    @Test
    @DisplayName("Generated password has correct length of 12 characters")
    void generatePassword_hasCorrectLength() {
        String password = PasswordGenerator.generatePassword();
        assertNotNull(password);
        assertEquals(12, password.length());
    }

    @Test
    @DisplayName("100% of 1,000 consecutive generated passwords satisfy enterprise password complexity policy")
    void generatePassword_satisfiesEnterpriseComplexityPolicyAcross1000Iterations() {
        Set<String> generatedPasswords = new HashSet<>();

        for (int i = 0; i < 1000; i++) {
            String password = PasswordGenerator.generatePassword();

            assertEquals(12, password.length(), "Password length must be exactly 12");
            assertTrue(ENTERPRISE_COMPLEXITY_PATTERN.matcher(password).matches(),
                    "Generated password must satisfy enterprise complexity regex: " + password);
            assertTrue(password.chars().anyMatch(Character::isUpperCase), "Must contain at least one uppercase letter");
            assertTrue(password.chars().anyMatch(Character::isLowerCase), "Must contain at least one lowercase letter");
            assertTrue(password.chars().anyMatch(Character::isDigit), "Must contain at least one digit");
            assertTrue(password.chars().anyMatch(ch -> "!@#$%&*".indexOf(ch) >= 0), "Must contain at least one special symbol");
            assertFalse(password.contains(" "), "Password must not contain whitespace");

            generatedPasswords.add(password);
        }

        // Entropy verification: 1,000 passwords generated with SecureRandom should have high uniqueness
        assertTrue(generatedPasswords.size() > 990, "Generated passwords should have high entropy and no systematic collisions");
    }
}
