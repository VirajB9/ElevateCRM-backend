package com.viraj.dmabackend.auth.bootstrap;

import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class PermissionBootstrap implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        for (PermissionType permissionType : PermissionType.values()) {
            String module = permissionType.name().contains("_")
                    ? permissionType.name().substring(0, permissionType.name().indexOf("_"))
                    : permissionType.name();

            java.util.Optional<Permission> existing = permissionRepository.findByPermissionType(permissionType);
            if (existing.isPresent()) {
                Permission p = existing.get();
                if (p.getModule() == null || !module.equals(p.getModule())) {
                    p.setModule(module);
                    permissionRepository.save(p);
                }
                continue;
            }

            Permission permission = Permission.builder()
                    .permissionType(permissionType)
                    .module(module)
                    .description(permissionType.name())
                    .build();

            permissionRepository.save(permission);
        }
    }
}
