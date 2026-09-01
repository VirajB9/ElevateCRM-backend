package com.viraj.dmabackend.auth.service;

import com.viraj.dmabackend.auth.dto.AuthenticationResponse;
import com.viraj.dmabackend.auth.dto.LoginRequest;

public interface AuthService {

    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
