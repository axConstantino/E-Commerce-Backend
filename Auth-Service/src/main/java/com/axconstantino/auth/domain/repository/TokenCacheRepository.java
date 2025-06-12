package com.axconstantino.auth.domain.repository;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.infrastructure.redis.model.TokenData;

import java.time.Duration;
import java.util.Optional;

public interface TokenCacheRepository {
    void save(String token, TokenData tokenData, Duration ttl);
    Optional<Token> find(String token);
    void delete(String token);
    void deleteAllForUser(String userId);
}
