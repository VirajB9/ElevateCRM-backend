package com.viraj.dmabackend.auth.security;

import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsTest {

    @Test
    void isEnabled_TrueWhenActive() {
        User user = User.builder().status(UserStatus.ACTIVE).email("user@agency.com").password("pass").build();
        CustomUserDetails details = new CustomUserDetails(user, Collections.emptyList());

        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isCredentialsNonExpired());
        assertEquals("user@agency.com", details.getUsername());
        assertEquals("pass", details.getPassword());
    }

    @Test
    void isEnabled_FalseWhenInactiveOrDeleted() {
        User inactive = User.builder().status(UserStatus.INACTIVE).build();
        CustomUserDetails inactiveDetails = new CustomUserDetails(inactive, Collections.emptyList());
        assertFalse(inactiveDetails.isEnabled());

        User deleted = User.builder().status(UserStatus.DELETED).build();
        CustomUserDetails deletedDetails = new CustomUserDetails(deleted, Collections.emptyList());
        assertFalse(deletedDetails.isEnabled());
    }

    @Test
    void isEnabled_FalseWhenDeletedIsTrue() {
        User activeButDeleted = User.builder().status(UserStatus.ACTIVE).build();
        activeButDeleted.setDeleted(true);
        CustomUserDetails details = new CustomUserDetails(activeButDeleted, Collections.emptyList());
        assertFalse(details.isEnabled());
    }

    @Test
    void isAccountNonLocked_FalseWhenStatusIsLocked() {
        User locked = User.builder()
                .status(UserStatus.LOCKED)
                .build();
        CustomUserDetails details = new CustomUserDetails(locked, Collections.emptyList());

        assertFalse(details.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_FalseWhenLockoutUntilIsInFuture() {
        User userWithLockout = User.builder()
                .status(UserStatus.ACTIVE)
                .lockoutUntil(LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(10))
                .build();
        CustomUserDetails details = new CustomUserDetails(userWithLockout, Collections.emptyList());

        assertFalse(details.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_TrueWhenLockoutUntilIsInPast() {
        User userWithExpiredLockout = User.builder()
                .status(UserStatus.ACTIVE)
                .lockoutUntil(LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(1))
                .build();
        CustomUserDetails details = new CustomUserDetails(userWithExpiredLockout, Collections.emptyList());

        assertTrue(details.isAccountNonLocked());
    }
}
