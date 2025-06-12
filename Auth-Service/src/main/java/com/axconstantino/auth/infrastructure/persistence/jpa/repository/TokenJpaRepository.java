package com.axconstantino.auth.infrastructure.persistence.jpa.repository;

import com.axconstantino.auth.infrastructure.persistence.jpa.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenJpaRepository extends JpaRepository<TokenEntity, UUID> {
    Optional<TokenEntity> findByToken(String token);
    List<TokenEntity> findAllByUserId(UUID userId);
}
