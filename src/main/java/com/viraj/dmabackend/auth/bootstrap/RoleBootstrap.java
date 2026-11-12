package com.viraj.dmabackend.auth.bootstrap;

import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class RoleBootstrap implements CommandLineRunner {
    private final PermissionRepository permissionRepository;

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createOwnerRole();
        createManagerRole();
        createEmployeeRole();
        createInternRole();
    }

    private void createOwnerRole() {
        List<String> allPermissionIds = getAllPermissionIds();
        Role existingOwner = roleRepository.findByName("OWNER").orElse(null);

        if (existingOwner != null) {
            if (existingOwner.getPermissionIds() == null
                    || !new java.util.HashSet<>(existingOwner.getPermissionIds()).containsAll(allPermissionIds)) {
                existingOwner.setPermissionIds(allPermissionIds);
                roleRepository.save(existingOwner);
            }
            return;
        }

        Role role = Role.builder()
                .name("OWNER")
                .description("SYSTEM OWNER")
                .permissionIds(allPermissionIds)
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private void createManagerRole() {
        List<String> permissionIds = getPermissionIds(
                PermissionType.USER_READ,

                PermissionType.ROLE_READ,

                PermissionType.MENU_READ,

                PermissionType.LEAD_CREATE,
                PermissionType.LEAD_READ,
                PermissionType.LEAD_UPDATE,
                PermissionType.LEAD_DELETE,
                PermissionType.LEAD_CONVERT,

                PermissionType.CLIENT_CREATE,
                PermissionType.CLIENT_READ,
                PermissionType.CLIENT_UPDATE,

                PermissionType.PROJECT_CREATE,
                PermissionType.PROJECT_READ,
                PermissionType.PROJECT_UPDATE,

                PermissionType.INVOICE_CREATE,
                PermissionType.INVOICE_READ,
                PermissionType.INVOICE_UPDATE
        );

        Role existing = roleRepository.findByName("MANAGER").orElse(null);
        if (existing != null) {
            if (existing.getPermissionIds() == null || existing.getPermissionIds().isEmpty()) {
                existing.setPermissionIds(permissionIds);
                roleRepository.save(existing);
            }
            return;
        }

        Role role = Role.builder()
                .name("MANAGER")
                .description("Agency Manager")
                .permissionIds(permissionIds)
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private void createEmployeeRole() {
        List<String> permissionIds = getPermissionIds(
                PermissionType.CLIENT_READ,
                PermissionType.LEAD_CREATE,
                PermissionType.LEAD_READ,
                PermissionType.PROJECT_READ,
                PermissionType.INVOICE_READ,
                PermissionType.MENU_READ
        );

        Role existing = roleRepository.findByName("EMPLOYEE").orElse(null);
        if (existing != null) {
            if (existing.getPermissionIds() == null || existing.getPermissionIds().isEmpty()) {
                existing.setPermissionIds(permissionIds);
                roleRepository.save(existing);
            }
            return;
        }

        Role role = Role.builder()
                .name("EMPLOYEE")
                .description("Agency Employee")
                .permissionIds(permissionIds)
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private void createInternRole() {
        List<String> permissionIds = getPermissionIds(
                PermissionType.CLIENT_READ,
                PermissionType.LEAD_READ,
                PermissionType.PROJECT_READ,
                PermissionType.MENU_READ
        );

        Role existing = roleRepository.findByName("INTERN").orElse(null);
        if (existing != null) {
            if (existing.getPermissionIds() == null || existing.getPermissionIds().isEmpty()) {
                existing.setPermissionIds(permissionIds);
                roleRepository.save(existing);
            }
            return;
        }

        Role role = Role.builder()
                .name("INTERN")
                .description("Agency Intern")
                .permissionIds(permissionIds)
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private List<String> getAllPermissionIds() {

        return permissionRepository.findAll()
                .stream()
                .map(Permission::getId)
                .toList();
    }

    private List<String> getPermissionIds(PermissionType... permissionTypes) {

        return Arrays.stream(permissionTypes)
                .map(permissionRepository::findByPermissionType)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Permission::getId)
                .toList();
    }
}
