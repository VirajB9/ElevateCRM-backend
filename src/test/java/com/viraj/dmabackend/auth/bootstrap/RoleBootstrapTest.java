package com.viraj.dmabackend.auth.bootstrap;

import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleBootstrapTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleBootstrap roleBootstrap;

    @Test
    void run_whenOwnerRoleDoesNotExist_createsOwnerWithAllPermissions() {
        Permission p1 = Permission.builder().id("p1").permissionType(PermissionType.USER_CREATE).build();
        when(permissionRepository.findAll()).thenReturn(List.of(p1));
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(Role.builder().name("MANAGER").build()));
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(Role.builder().name("EMPLOYEE").build()));
        when(roleRepository.findByName("INTERN")).thenReturn(Optional.of(Role.builder().name("INTERN").build()));

        roleBootstrap.run();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeastOnce()).save(captor.capture());
        Role savedOwner = captor.getAllValues().stream()
                .filter(r -> "OWNER".equals(r.getName()))
                .findFirst()
                .orElseThrow();

        assertThat(savedOwner.getPermissionIds()).containsExactly("p1");
    }

    @Test
    void run_whenOwnerRoleExistsButLacksNewPermissions_synchronizesPermissions() {
        Permission p1 = Permission.builder().id("p1").permissionType(PermissionType.USER_CREATE).build();
        Permission p2 = Permission.builder().id("p2").permissionType(PermissionType.USER_READ).build();
        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));

        Role existingOwner = Role.builder()
                .id("owner-1")
                .name("OWNER")
                .permissionIds(new ArrayList<>(List.of("p1")))
                .build();

        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(existingOwner));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(Role.builder().name("MANAGER").build()));
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.of(Role.builder().name("EMPLOYEE").build()));
        when(roleRepository.findByName("INTERN")).thenReturn(Optional.of(Role.builder().name("INTERN").build()));

        roleBootstrap.run();

        verify(roleRepository).save(existingOwner);
        assertThat(existingOwner.getPermissionIds()).containsExactlyInAnyOrder("p1", "p2");
    }
}
