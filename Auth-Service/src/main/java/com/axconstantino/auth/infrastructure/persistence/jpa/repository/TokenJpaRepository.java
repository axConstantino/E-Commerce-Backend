package com.axconstantino.auth.infrastructure.persistence.jpa.repository;

import com.axconstantino.auth.infrastructure.persistence.jpa.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenJpaRepository extends JpaRepository<TokenEntity, UUID> {
    Optional<TokenEntity> findByTokenValue(String tokenValue);

    @Query("SELECT t FROM TokenEntity t WHERE t.user.id = :userId AND t.active = true")
    List<TokenEntity> findByUserIdAndActiveTrue(UUID userId);
}
