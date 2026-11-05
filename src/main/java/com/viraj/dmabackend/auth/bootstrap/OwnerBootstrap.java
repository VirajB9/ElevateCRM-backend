package com.viraj.dmabackend.auth.bootstrap;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.repository.RoleRepository;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.config.OwnerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class OwnerBootstrap implements CommandLineRunner {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final OwnerProperties ownerProperties;

    @Override
    public void run(String... args) throws Exception {

        String ownerEmail = ownerProperties.getEmail() != null
                ? ownerProperties.getEmail().trim().toLowerCase()
                : null;
        String initialPassword = ownerProperties.getInitialPassword();

        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new IllegalStateException("OWNER_EMAIL must be configured");
        }

        if (userRepository.findByEmail(ownerEmail).isPresent()) {
            return;
        }

        if (initialPassword == null || initialPassword.isBlank()) {
            throw new IllegalStateException(
                    "OWNER_INITIAL_PASSWORD must be configured"
            );
        }

        Role ownerRole = roleRepository.findByName("OWNER")
                .orElseThrow(() -> new RuntimeException("OWNER role not found"));

        User owner = User.builder()
                .firstName("Owner")
                .lastName("Admin")
                .email(ownerEmail)
                .phoneNumber("9999999999")
                .password(passwordEncoder.encode(initialPassword))
                .passwordChangeRequired(true)
                .roleId(ownerRole.getId())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(owner);
    }
}
