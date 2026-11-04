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
        User user = userRepository
                .findByEmailAndStatusNot(email, com.viraj.dmabackend.auth.enums.UserStatus.DELETED)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Role role = roleRepository
                .findById(user.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        List<Permission> permissions =
                permissionRepository.findAllById(role.getPermissionIds());

        List<SimpleGrantedAuthority> authorities =
                permissions.stream()
                        .map(permission ->
                                new SimpleGrantedAuthority(
                                        permission.getPermissionType()
                                                .name()
                                                .toLowerCase()
                                                .replace("_", ":")
                                )
                        )
                        .toList();

        return new CustomUserDetails(
                user,
                authorities
        );
    }
}
