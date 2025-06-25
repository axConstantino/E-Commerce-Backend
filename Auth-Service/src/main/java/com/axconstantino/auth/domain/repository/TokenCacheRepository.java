package com.axconstantino.auth.domain.repository;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.infrastructure.redis.model.TokenData;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface TokenCacheRepository {
    void save(TokenData tokenData, long ttl, TimeUnit unit);
    Optional<Token> find(String token);
    void delete(String token);
    void deleteAllForUser(String userId);
}
