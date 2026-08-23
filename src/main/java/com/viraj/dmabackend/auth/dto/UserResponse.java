package com.viraj.dmabackend.auth.dto;

import com.viraj.dmabackend.auth.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
public class UserResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String roleName;

    private UserStatus status;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}
