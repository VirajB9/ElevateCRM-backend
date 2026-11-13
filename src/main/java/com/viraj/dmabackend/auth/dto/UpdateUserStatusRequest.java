package com.viraj.dmabackend.auth.dto;

import com.viraj.dmabackend.auth.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull
    private UserStatus status;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;
}
