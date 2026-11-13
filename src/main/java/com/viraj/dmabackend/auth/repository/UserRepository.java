package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {

    @Query("{ 'email': ?0, 'deleted': false }")
    Optional<User> findByEmail(String email);

    @Query("{ 'email': ?0, 'deleted': true }")
    Optional<User> findDeletedByEmail(String email);

    @Query("{ 'email': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<User> findByEmailAndStatusNot(String email, UserStatus status);

    @Query(value = "{ 'email': ?0, 'deleted': false }", exists = true)
    boolean existsByEmail(String email);

    @Query(value = "{ 'email': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByEmailAndStatusNot(String email, UserStatus status);

    @Query(value = "{ 'phoneNumber': ?0, 'deleted': false }", exists = true)
    boolean existsByPhoneNumber(String phoneNumber);

    @Query(value = "{ 'phoneNumber': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, UserStatus status);

    @Query("{ 'status': ?0, 'deleted': false }")
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query(value = "{ 'phoneNumber': ?0, '_id': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, String id);

    @Query(value = "{ 'phoneNumber': ?0, '_id': { $ne: ?1 }, 'status': { $ne: ?2 }, 'deleted': false }", exists = true)
    boolean existsByPhoneNumberAndIdNotAndStatusNot(String phoneNumber, String id, UserStatus status);

    @Query("{ 'roleId': ?0, 'deleted': false }")
    Page<User> findByRoleId(String roleId, Pageable pageable);

    @Query("{ 'roleId': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Page<User> findByRoleIdAndStatusNot(String roleId, UserStatus status, Pageable pageable);

    @Query("{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }")
    Optional<User> findByIdAndStatusNot(String id, UserStatus status);

    @Query(value = "{ '_id': ?0, 'status': { $ne: ?1 }, 'deleted': false }", exists = true)
    boolean existsByIdAndStatusNot(String id, UserStatus status);

    @Query("{ 'status': { $ne: ?0 }, 'deleted': false }")
    Page<User> findByStatusNot(UserStatus status, Pageable pageable);
}
