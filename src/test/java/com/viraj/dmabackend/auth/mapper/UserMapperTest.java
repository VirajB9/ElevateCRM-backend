package com.viraj.dmabackend.auth.mapper;

import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toUserResponse_ReturnsNullWhenUserIsNull() {
        assertNull(userMapper.toUserResponse(null, "ADMIN"));
    }

    @Test
    void toUserResponse_MapsFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id("u-1")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("9876543210")
                .roleId("role-admin")
                .status(UserStatus.ACTIVE)
                .build();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setCreatedBy("creator@example.com");
        user.setUpdatedBy("modifier@example.com");

        UserResponse response = userMapper.toUserResponse(user, "ADMIN");

        assertNotNull(response);
        assertEquals("u-1", response.getId());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhoneNumber());
        assertEquals("role-admin", response.getRoleId());
        assertEquals("ADMIN", response.getRoleName());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
        assertEquals("creator@example.com", response.getCreatedBy());
        assertEquals("modifier@example.com", response.getUpdatedBy());
    }
}
