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

    @Test
    void run_createsManagerAndEmployeeWithSpecCompliantPermissions() {
        Permission pUserRead = Permission.builder().id("p-ur").permissionType(PermissionType.USER_READ).build();
        Permission pLeadCreate = Permission.builder().id("p-lc").permissionType(PermissionType.LEAD_CREATE).build();
        Permission pLeadDelete = Permission.builder().id("p-ld").permissionType(PermissionType.LEAD_DELETE).build();
        Permission pInvoiceCreate = Permission.builder().id("p-ic").permissionType(PermissionType.INVOICE_CREATE).build();
        Permission pProjectUpdate = Permission.builder().id("p-pu").permissionType(PermissionType.PROJECT_UPDATE).build();

        when(permissionRepository.findAll()).thenReturn(List.of(pUserRead, pLeadCreate, pLeadDelete, pInvoiceCreate, pProjectUpdate));
        lenient().when(permissionRepository.findByPermissionType(any())).thenAnswer(invocation -> {
            PermissionType pt = invocation.getArgument(0);
            return switch (pt) {
                case USER_READ -> Optional.of(pUserRead);
                case LEAD_CREATE -> Optional.of(pLeadCreate);
                case LEAD_DELETE -> Optional.of(pLeadDelete);
                case INVOICE_CREATE -> Optional.of(pInvoiceCreate);
                case PROJECT_UPDATE -> Optional.of(pProjectUpdate);
                default -> Optional.empty();
            };
        });

        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(Role.builder().name("OWNER").permissionIds(List.of("p-ur")).build()));
        when(roleRepository.findByName("MANAGER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("EMPLOYEE")).thenReturn(Optional.empty());
        when(roleRepository.findByName("INTERN")).thenReturn(Optional.empty());

        roleBootstrap.run();

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, atLeastOnce()).save(captor.capture());

        Role manager = captor.getAllValues().stream().filter(r -> "MANAGER".equals(r.getName())).findFirst().orElseThrow();
        Role employee = captor.getAllValues().stream().filter(r -> "EMPLOYEE".equals(r.getName())).findFirst().orElseThrow();

        // Manager should have USER_READ, LEAD_DELETE, INVOICE_CREATE, PROJECT_UPDATE
        assertThat(manager.getPermissionIds()).contains("p-ur", "p-ld", "p-ic", "p-pu");
        // Employee should have LEAD_CREATE, but NOT PROJECT_UPDATE or LEAD_DELETE
        assertThat(employee.getPermissionIds()).contains("p-lc");
        assertThat(employee.getPermissionIds()).doesNotContain("p-pu", "p-ld");
    }
}
