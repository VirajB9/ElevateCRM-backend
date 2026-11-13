package com.viraj.dmabackend.auth.security;

import com.viraj.dmabackend.auth.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        boolean isTemporarilyLocked = user.getLockoutUntil() != null
                && user.getLockoutUntil().isAfter(LocalDateTime.now(ZoneOffset.UTC));
        return user.getStatus() != com.viraj.dmabackend.auth.enums.UserStatus.LOCKED && !isTemporarilyLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == com.viraj.dmabackend.auth.enums.UserStatus.ACTIVE && !user.isDeleted();
    }
}
