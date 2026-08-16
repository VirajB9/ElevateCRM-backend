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
        if (roleRepository.findByName("OWNER").isPresent()) {
            return;
        }
        Role role = Role.builder()
                .name("OWNER")
                .description("SYSTEM OWNER")
                .permissionIds(getAllPermissionIds())
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private void createManagerRole() {

        if (roleRepository.findByName("MANAGER").isPresent()) {
            return;
        }

        Role role = Role.builder()
                .name("MANAGER")
                .description("Agency Manager")
                .permissionIds(getPermissionIds(
                        PermissionType.USER_CREATE,
                        PermissionType.USER_READ,
                        PermissionType.USER_UPDATE,

                        PermissionType.CLIENT_CREATE,
                        PermissionType.CLIENT_READ,
                        PermissionType.CLIENT_UPDATE,

                        PermissionType.LEAD_CREATE,
                        PermissionType.LEAD_READ,
                        PermissionType.LEAD_UPDATE,

                        PermissionType.PROJECT_CREATE,
                        PermissionType.PROJECT_READ,
                        PermissionType.PROJECT_UPDATE,

                        PermissionType.INVOICE_READ,

                        PermissionType.ROLE_READ,

                        PermissionType.MENU_READ,

                        PermissionType.LEAD_CONVERT
                ))
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private void createEmployeeRole() {

        if (roleRepository.findByName("EMPLOYEE").isPresent()) {
            return;
        }

        Role role = Role.builder()
                .name("EMPLOYEE")
                .description("Agency Employee")
                .permissionIds(getPermissionIds(
                        PermissionType.CLIENT_READ,
                        PermissionType.LEAD_READ,
                        PermissionType.PROJECT_READ,
                        PermissionType.PROJECT_UPDATE,
                        PermissionType.INVOICE_READ,
                        PermissionType.MENU_READ
                ))
                .systemRole(true)
                .build();

        roleRepository.save(role);
    }

    private void createInternRole() {

        if (roleRepository.findByName("INTERN").isPresent()) {
            return;
        }

        Role role = Role.builder()
                .name("INTERN")
                .description("Agency Intern")
                .permissionIds(getPermissionIds(
                        PermissionType.CLIENT_READ,
                        PermissionType.LEAD_READ,
                        PermissionType.PROJECT_READ,
                        PermissionType.MENU_READ
                ))
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
