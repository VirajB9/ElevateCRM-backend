package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatusNot(String email, UserStatus status);

    boolean existsByEmail(String email);

    boolean existsByEmailAndStatusNot(String email, UserStatus status);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String id);

    boolean existsByPhoneNumberAndIdNotAndStatusNot(String phoneNumber, String id, UserStatus status);

    Page<User> findByRoleId(String roleId, Pageable pageable);

    Page<User> findByRoleIdAndStatusNot(String roleId, UserStatus status, Pageable pageable);

    Optional<User> findByIdAndStatusNot(String id, UserStatus status);

    boolean existsByIdAndStatusNot(String id, UserStatus status);

    Page<User> findByStatusNot(UserStatus status, Pageable pageable);
}
