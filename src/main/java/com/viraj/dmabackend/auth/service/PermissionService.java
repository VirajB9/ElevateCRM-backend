package com.viraj.dmabackend.auth.service;

import com.viraj.dmabackend.auth.dto.PermissionResponse;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> getAllPermissions();
}
