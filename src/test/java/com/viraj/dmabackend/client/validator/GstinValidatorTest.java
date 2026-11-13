package com.viraj.dmabackend.client.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GstinValidatorTest {

    @Test
    void isValidGstin_WithValidGstin_ReturnsTrue() {
        // Valid Indian GSTINs with verified Modulo-36 check digits
        assertTrue(GstinValidator.isValidGstin("27AAPFU0939F1ZV"));
        assertTrue(GstinValidator.isValidGstin("27AABCU9603R1ZN"));
        assertTrue(GstinValidator.isValidGstin("27ABCDE1234F1Z0"));
        assertTrue(GstinValidator.isValidGstin("97ABCDE1234F1ZT"));
        assertTrue(GstinValidator.isValidGstin("99ABCDE1234F1ZP"));
    }

    @Test
    void isValidGstin_WithInvalidChecksum_ReturnsFalse() {
        // 15th character altered from V to X
        assertFalse(GstinValidator.isValidGstin("27AAPFU0939F1ZX"));
        // 15th character altered from 0 to 5
        assertFalse(GstinValidator.isValidGstin("27ABCDE1234F1Z5"));
        // 15th character altered from N to M
        assertFalse(GstinValidator.isValidGstin("27AABCU9603R1ZM"));
    }

    @Test
    void isValidGstin_WithInvalidFormat_ReturnsFalse() {
        assertFalse(GstinValidator.isValidGstin("INVALID_GST"));
        assertFalse(GstinValidator.isValidGstin("00AAPFU0939F1ZV")); // State code 00 is invalid
        assertFalse(GstinValidator.isValidGstin("45AAPFU0939F1ZV")); // State code 45 is invalid
        assertFalse(GstinValidator.isValidGstin("27AAPFU0939F11V")); // 14th char must be Z
        assertFalse(GstinValidator.isValidGstin("27AAPFU0939F1Z"));  // 14 characters instead of 15
        assertFalse(GstinValidator.isValidGstin("27AAPFU0939F1ZVV")); // 16 characters
    }

    @Test
    void isValidGstin_WithNullOrBlank_ReturnsTrue() {
        assertTrue(GstinValidator.isValidGstin(null));
        assertTrue(GstinValidator.isValidGstin(""));
        assertTrue(GstinValidator.isValidGstin("   "));
    }

    @Test
    void calculateCheckDigit_ComputesStatutoryChecksum() {
        assertEquals('V', GstinValidator.calculateCheckDigit("27AAPFU0939F1Z"));
        assertEquals('N', GstinValidator.calculateCheckDigit("27AABCU9603R1Z"));
        assertEquals('0', GstinValidator.calculateCheckDigit("27ABCDE1234F1Z"));
        assertEquals('T', GstinValidator.calculateCheckDigit("97ABCDE1234F1Z"));
        assertEquals('P', GstinValidator.calculateCheckDigit("99ABCDE1234F1Z"));
    }

    @Test
    void calculateCheckDigit_WithInvalidLengthOrChars_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> GstinValidator.calculateCheckDigit(null));
        assertThrows(IllegalArgumentException.class, () -> GstinValidator.calculateCheckDigit("27AAPFU0939"));
        assertThrows(IllegalArgumentException.class, () -> GstinValidator.calculateCheckDigit("27AAPFU0939F1Z!@"));
    }
}
