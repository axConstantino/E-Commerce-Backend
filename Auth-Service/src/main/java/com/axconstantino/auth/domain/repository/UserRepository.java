package com.axconstantino.auth.domain.repository;

import com.axconstantino.auth.domain.model.User;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);

    @Cacheable(value = "users", key = "#email")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void save(User user);

}

