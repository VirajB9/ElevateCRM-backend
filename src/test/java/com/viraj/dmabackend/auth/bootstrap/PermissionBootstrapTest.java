package com.viraj.dmabackend.auth.bootstrap;

import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionBootstrapTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionBootstrap permissionBootstrap;

    @Test
    void run_PopulatesModuleFieldWhenCreatingPermissions() {
        when(permissionRepository.findByPermissionType(any())).thenReturn(Optional.empty());

        permissionBootstrap.run();

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository, atLeastOnce()).save(captor.capture());

        for (Permission saved : captor.getAllValues()) {
            assertThat(saved.getModule()).isNotNull();
            assertThat(saved.getModule()).isNotBlank();
            assertThat(saved.getPermissionType().name()).startsWith(saved.getModule());
        }
    }

    @Test
    void run_SelfHealsExistingPermissionsWithoutModule() {
        Permission existingWithoutModule = Permission.builder()
                .id("p-1")
                .permissionType(PermissionType.USER_READ)
                .module(null)
                .build();

        lenient().when(permissionRepository.findByPermissionType(any())).thenReturn(Optional.empty());
        lenient().when(permissionRepository.findByPermissionType(PermissionType.USER_READ))
                .thenReturn(Optional.of(existingWithoutModule));

        permissionBootstrap.run();

        assertThat(existingWithoutModule.getModule()).isEqualTo("USER");
        verify(permissionRepository).save(existingWithoutModule);
    }
}
