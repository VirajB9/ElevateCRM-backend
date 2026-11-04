package com.viraj.dmabackend.auth.service.impl;

import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.mapper.RoleMapper;
import com.viraj.dmabackend.auth.mapper.UserMapper;
import com.viraj.dmabackend.auth.repository.PermissionRepository;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.auth.validator.RoleValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleValidator roleValidator;

    @InjectMocks
    private RoleServiceImpl roleService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void getUsersByRole_ExcludesDeletedUsers() {
        Role role = Role.builder().id("role-1").name("EMPLOYEE").build();
        User user = User.builder().id("user-1").email("emp@agency.com").roleId("role-1").status(UserStatus.ACTIVE).build();

        when(roleRepository.findById("role-1")).thenReturn(Optional.of(role));
        when(userRepository.findByRoleIdAndStatusNot(eq("role-1"), eq(UserStatus.DELETED), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(any(User.class), eq("EMPLOYEE")))
                .thenReturn(UserResponse.builder().id("user-1").email("emp@agency.com").roleName("EMPLOYEE").build());

        Page<UserResponse> response = roleService.getUsersByRole("role-1", pageable);

        assertEquals(1, response.getTotalElements());
        verify(userRepository).findByRoleIdAndStatusNot(eq("role-1"), eq(UserStatus.DELETED), eq(pageable));
    }
}
