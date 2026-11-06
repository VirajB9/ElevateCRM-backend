package com.viraj.dmabackend.auth.security;


import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
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

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        User user = userRepository
                .findByEmailAndStatusNot(normalizedEmail, com.viraj.dmabackend.auth.enums.UserStatus.DELETED)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Role not found for user: " + email));

        List<String> permissionIds = role.getPermissionIds() != null
                ? role.getPermissionIds()
                : java.util.Collections.emptyList();

        List<Permission> permissions =
                permissionRepository.findAllById(permissionIds);

        List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        if (role.getName() != null && !role.getName().isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().trim()));
        }

        permissions.stream()
                .filter(permission -> permission != null && permission.getPermissionType() != null)
                .map(permission ->
                        new SimpleGrantedAuthority(
                                permission.getPermissionType()
                                        .name()
                                        .toLowerCase()
                                        .replace("_", ":")
                        )
                )
                .forEach(authorities::add);

        return new CustomUserDetails(
                user,
                authorities
        );
    }
}
