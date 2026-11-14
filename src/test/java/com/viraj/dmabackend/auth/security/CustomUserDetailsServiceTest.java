package com.viraj.dmabackend.auth.security;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.dto.RoleResponse;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.RoleNotFoundException;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.service.RoleService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

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
        when(roleService.getRoleById("r1")).thenThrow(new RoleNotFoundException("r1"));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("user@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Role not found for user");
    }

    @Test
    void loadUserByUsername_whenRoleHasNullPermissions_returnsUserDetailsWithRoleAuthority() {
        User user = User.builder().id("u1").email("user@example.com").roleId("r1").status(UserStatus.ACTIVE).build();
        RoleResponse role = RoleResponse.builder().id("r1").name("EMPLOYEE").permissions(null).build();

        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(roleService.getRoleById("r1")).thenReturn(role);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_EMPLOYEE");
        verify(roleService).getRoleById("r1");
    }

    @Test
    void loadUserByUsername_whenValidUserAndPermissions_returnsAuthorities() {
        User user = User.builder().id("u1").email("user@example.com").roleId("r1").status(UserStatus.ACTIVE).build();
        RoleResponse role = RoleResponse.builder().id("r1").name("MANAGER")
                .permissions(List.of(PermissionResponse.builder().id("p1").name("LEAD_READ").build()))
                .build();

        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(roleService.getRoleById("r1")).thenReturn(role);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_MANAGER", "lead:read");
        verify(roleService).getRoleById("r1");
    }

    @Test
    void loadUserByUsername_normalizesEmailCasingAndWhitespace() {
        User user = User.builder().id("u1").email("user@example.com").roleId("r1").status(UserStatus.ACTIVE).build();
        RoleResponse role = RoleResponse.builder().id("r1").name("EMPLOYEE").permissions(Collections.emptyList()).build();

        when(userRepository.findByEmailAndStatusNot("user@example.com", UserStatus.DELETED))
                .thenReturn(Optional.of(user));
        when(roleService.getRoleById("r1")).thenReturn(role);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("  User@Example.COM  ");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("user@example.com");
    }
}
