package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.enums.PermissionType;
import com.viraj.dmabackend.auth.entity.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermissionRepository extends MongoRepository<Permission, String> {

    boolean existsByPermissionType(PermissionType permissionType);

    Optional<Permission> findByPermissionType(PermissionType permissionType);
}