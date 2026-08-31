package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String id);

    Page<User> findByRoleId(String roleId, Pageable pageable);
}
