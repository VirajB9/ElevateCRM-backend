package com.viraj.dmabackend.auth.repository;

import com.viraj.dmabackend.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> searchUsers(String keyword, Pageable pageable);
}