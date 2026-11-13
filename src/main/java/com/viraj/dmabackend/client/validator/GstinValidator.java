package com.viraj.dmabackend.client.validator;

import java.util.regex.Pattern;

public final class GstinValidator {

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Pattern GSTIN_PATTERN = Pattern.compile(
            "^(0[1-9]|[1-2][0-9]|3[0-8]|97|99)[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
    );

    private GstinValidator() {}

    /**
     * Validates a GSTIN against the statutory Indian Goods and Services Tax Network (GSTN)
     * 15-character format and Modulo-36 check-digit verification algorithm.
     *
     * @param gstin the input GSTIN string
     * @return true if null, empty, or a valid 15-character GSTIN with correct checksum; false otherwise
     */
    public static boolean isValidGstin(String gstin) {
        if (gstin == null) {
            return true;
        }
        String trimmed = gstin.trim().toUpperCase();
        if (trimmed.isEmpty()) {
            return true;
        }

        if (!GSTIN_PATTERN.matcher(trimmed).matches()) {
            return false;
        }

        try {
            return trimmed.charAt(14) == calculateCheckDigit(trimmed.substring(0, 14));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Computes the 15th statutory Luhn Modulo-36 check character from the first 14 characters of a GSTIN.
     *
     * @param gstin14 the first 14 characters of the GSTIN
     * @return the calculated check character
     */
    public static char calculateCheckDigit(String gstin14) {
        if (gstin14 == null || gstin14.length() != 14) {
            throw new IllegalArgumentException("Input must be exactly 14 characters long");
        }

        int total = 0;
        int mod = CHARS.length();

        for (int i = 0; i < 14; i++) {
            char ch = Character.toUpperCase(gstin14.charAt(i));
            int digitVal = CHARS.indexOf(ch);
            if (digitVal < 0) {
                throw new IllegalArgumentException("Invalid character '" + ch + "' in GSTIN");
            }
            int factor = (i % 2 == 0) ? 1 : 2;
            int product = factor * digitVal;
            total += (product / mod) + (product % mod);
        }

        int remainder = total % mod;
        int checkIndex = (mod - remainder) % mod;
        return CHARS.charAt(checkIndex);
    }
}
