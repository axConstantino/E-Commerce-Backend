package com.axconstantino.auth.domain.repository;

import com.axconstantino.auth.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    Page<User> findAll(Pageable pageable);

    void save(User user);

    void deleteById(UUID id);
}

