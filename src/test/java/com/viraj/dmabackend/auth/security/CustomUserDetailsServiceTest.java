package com.viraj.dmabackend.auth.security;

import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmailAndStatusNot("missing@example.com", UserStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void loadUserByUsername_whenRoleNotFound_throwsUsernameNotFoundException() {
        User user = User.builder().id("u1").email("user@example.com").roleId("r1").status(UserStatus.ACTIVE).build();
        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById("r1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("user@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Role not found for user");
    }

    @Test
    void loadUserByUsername_whenRoleHasNullPermissionIds_returnsUserDetailsWithEmptyAuthorities() {
        User user = User.builder().id("u1").email("user@example.com").roleId("r1").status(UserStatus.ACTIVE).build();
        Role role = Role.builder().id("r1").name("EMPLOYEE").permissionIds(null).build();

        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById("r1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_whenValidUserAndPermissions_returnsAuthorities() {
        User user = User.builder().id("u1").email("user@example.com").roleId("r1").status(UserStatus.ACTIVE).build();
        Role role = Role.builder().id("r1").name("MANAGER").permissionIds(List.of("p1")).build();
        Permission permission = Permission.builder().id("p1").permissionType(PermissionType.LEAD_READ).build();

        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(roleRepository.findById("r1")).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(List.of("p1"))).thenReturn(List.of(permission));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).extracting("authority")
                .containsExactly("lead:read");
    }
}
