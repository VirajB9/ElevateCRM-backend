package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.PermissionResponse;
import com.viraj.dmabackend.auth.entity.Permission;
import com.viraj.dmabackend.auth.mapper.PermissionMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Cacheable("permissions")
    @Override
    public List<PermissionResponse> getAllPermissions() {

        List<Permission> permissions = permissionRepository.findAll();

        if (permissions.size() > 500) {
            log.warn("Permission count exceeds expected threshold: {}", permissions.size());
        }

        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }
}
