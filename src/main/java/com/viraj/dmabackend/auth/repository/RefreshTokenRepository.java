package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
}