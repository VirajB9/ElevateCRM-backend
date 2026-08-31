package com.viraj.dmabackend.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateUserResponse {

    private UserResponse user;

    private String temporaryPassword;
}
