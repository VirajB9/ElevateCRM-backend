package com.viraj.dmabackend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;

    @Builder.Default
    private String type="Bearer";

    private String refreshToken;

    private UserResponse user;
}
