package com.axconstantino.auth.infrastructure.persistence.jpa.repository;

import com.axconstantino.auth.infrastructure.persistence.jpa.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TokenJpaRepository extends JpaRepository<TokenEntity, UUID> {
    List<TokenEntity> findAllByUserId(UUID userId);
    List<TokenEntity> findAllValidTokensByUser(UUID userId);
}
