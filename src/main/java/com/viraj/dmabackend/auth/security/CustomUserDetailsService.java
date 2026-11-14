package com.viraj.dmabackend.auth.security;

import com.viraj.dmabackend.auth.dto.RoleResponse;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.RoleNotFoundException;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        User user = userRepository
                .findByEmailAndStatusNot(normalizedEmail, com.viraj.dmabackend.auth.enums.UserStatus.DELETED)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        RoleResponse role;
        try {
            role = roleService.getRoleById(user.getRoleId());
        } catch (RoleNotFoundException ex) {
            throw new UsernameNotFoundException("Role not found for user: " + email, ex);
        }

        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        if (role.getName() != null && !role.getName().isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().trim()));
        }

        if (role.getPermissions() != null) {
            role.getPermissions().stream()
                    .filter(permission -> permission != null && permission.getName() != null && !permission.getName().isBlank())
                    .map(permission ->
                            new SimpleGrantedAuthority(
                                    permission.getName()
                                            .toLowerCase()
                                            .replace("_", ":")
                            )
                    )
                    .sorted(java.util.Comparator.comparing(SimpleGrantedAuthority::getAuthority))
                    .forEach(authorities::add);
        }

        return new CustomUserDetails(
                user,
                authorities
        );
    }
}
