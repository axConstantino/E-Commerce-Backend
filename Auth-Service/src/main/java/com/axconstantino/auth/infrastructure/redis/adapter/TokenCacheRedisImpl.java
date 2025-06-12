package com.axconstantino.auth.infrastructure.redis.adapter;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.repository.TokenCacheRepository;
import com.axconstantino.auth.infrastructure.persistence.mapper.RedisTokenMapper;
import com.axconstantino.auth.infrastructure.redis.model.RedisTokenEntity;
import com.axconstantino.auth.infrastructure.redis.model.TokenData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class TokenCacheRedisImpl implements TokenCacheRepository {

    private final RedisTemplate<String, RedisTokenEntity> redisTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;
    private final RedisTokenMapper mapper;


    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String USER_PREFIX = "auth:user:";

    @Override
    public void save(String token, TokenData tokenData, Duration ttl) {
        RedisTokenEntity entity = RedisTokenEntity.builder()
                .token(token)
                .userId(tokenData.userId())
                .active(tokenData.active())
                .expiresAt(tokenData.expiresAt())
                .ipAddress(tokenData.ipAddress())
                .userAgent(tokenData.userAgent())
                .build();

        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, entity, ttl);
        redisStringTemplate.opsForSet().add(USER_PREFIX + tokenData.userId(), token);
    }

    @Override
    public Optional<Token> find(String token) {
        RedisTokenEntity data = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        return Optional.ofNullable(data)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    @Override
    public void deleteAllForUser(String userId) {
        String userKey = USER_PREFIX + userId;
        Set<RedisTokenEntity> tokens = redisTemplate.opsForSet().members(userKey);
        if (tokens != null) {
            tokens.forEach(t -> redisTemplate.delete(TOKEN_PREFIX + t));
        }
        redisTemplate.delete(userKey);
    }
}
