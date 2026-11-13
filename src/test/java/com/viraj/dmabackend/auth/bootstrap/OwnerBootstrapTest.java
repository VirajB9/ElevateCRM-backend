package com.viraj.dmabackend.auth.bootstrap;

import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.config.OwnerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerBootstrapTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OwnerProperties ownerProperties;

    @InjectMocks
    private OwnerBootstrap ownerBootstrap;

    private Role ownerRole;

    @BeforeEach
    void setUp() {
        ownerRole = Role.builder().id("role-owner-id").name("OWNER").build();
        when(ownerProperties.getEmail()).thenReturn("owner@agency.com");
    }

    @Test
    void run_whenOwnerDoesNotExist_createsOwner() throws Exception {
        when(ownerProperties.getInitialPassword()).thenReturn("Secret123!");
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmail("owner@agency.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123!")).thenReturn("encodedPassword");

        ownerBootstrap.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedOwner = captor.getValue();
        assertThat(savedOwner.getEmail()).isEqualTo("owner@agency.com");
        assertThat(savedOwner.getRoleId()).isEqualTo("role-owner-id");
        assertThat(savedOwner.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedOwner.getPhoneNumber()).isEqualTo("9999999999");
    }

    @Test
    void run_whenCustomPhoneNumberProvided_createsOwnerWithConfiguredPhone() throws Exception {
        when(ownerProperties.getInitialPassword()).thenReturn("Secret123!");
        when(ownerProperties.getPhoneNumber()).thenReturn("9876543210");
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(ownerRole));
        when(userRepository.findByEmail("owner@agency.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123!")).thenReturn("encodedPassword");

        ownerBootstrap.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedOwner = captor.getValue();
        assertThat(savedOwner.getPhoneNumber()).isEqualTo("9876543210");
    }

    @Test
    void run_whenOwnerExistsWithCorruptedRoleIdAndInactiveStatus_selfHeals() throws Exception {
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(ownerRole));

        User corruptedOwner = User.builder()
                .id("user-owner-1")
                .email("owner@agency.com")
                .roleId("wrong-role-id")
                .status(UserStatus.INACTIVE)
                .build();
        when(userRepository.findByEmail("owner@agency.com")).thenReturn(Optional.of(corruptedOwner));

        ownerBootstrap.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User healedOwner = captor.getValue();
        assertThat(healedOwner.getRoleId()).isEqualTo("role-owner-id");
        assertThat(healedOwner.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void run_whenOwnerExistsAndIsValid_doesNotSave() throws Exception {
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(ownerRole));

        User validOwner = User.builder()
                .id("user-owner-1")
                .email("owner@agency.com")
                .roleId("role-owner-id")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail("owner@agency.com")).thenReturn(Optional.of(validOwner));

        ownerBootstrap.run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_whenOwnerSoftDeleted_selfHealsAndResetsDeletedFlag() throws Exception {
        when(roleRepository.findByName("OWNER")).thenReturn(Optional.of(ownerRole));

        User softDeletedOwner = User.builder()
                .id("user-owner-1")
                .email("owner@agency.com")
                .roleId("role-owner-id")
                .status(UserStatus.DELETED)
                .build();
        softDeletedOwner.setDeleted(true);

        when(userRepository.findByEmail("owner@agency.com")).thenReturn(Optional.empty());
        when(userRepository.findDeletedByEmail("owner@agency.com")).thenReturn(Optional.of(softDeletedOwner));

        ownerBootstrap.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User healedOwner = captor.getValue();
        assertThat(healedOwner.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(healedOwner.isDeleted()).isFalse();
    }
}
