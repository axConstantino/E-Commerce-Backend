package com.axconstantino.auth.infrastructure.redis.adapter;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.repository.TokenCacheRepository;
import com.axconstantino.auth.infrastructure.persistence.mapper.RedisTokenMapper;
import com.axconstantino.auth.infrastructure.redis.model.RedisTokenEntity;
import com.axconstantino.auth.infrastructure.redis.model.TokenData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TokenCacheRedisImpl implements TokenCacheRepository {

    private final RedisTemplate<String, RedisTokenEntity> redisTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;
    private final RedisTokenMapper mapper;


    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String USER_PREFIX = "auth:user:";

    @Override
    public void save(TokenData tokenData, long ttl, TimeUnit unit) {
        String tokenKey = TOKEN_PREFIX + tokenData.tokenValue();
        String userKey = USER_PREFIX + tokenData.userId();

        RedisTokenEntity entity = RedisTokenEntity.builder()
                .token(tokenData.tokenValue())
                .userId(tokenData.userId())
                .active(tokenData.active())
                .expiresAt(tokenData.expiresAt())
                .ipAddress(tokenData.ipAddress())
                .userAgent(tokenData.userAgent())
                .build();

        redisTemplate.opsForValue().set(tokenKey, entity, ttl, unit);
        redisStringTemplate.opsForSet().add(userKey, tokenData.tokenValue());
        redisStringTemplate.expire(userKey, ttl, unit);
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
        Set<String> tokens = redisStringTemplate.opsForSet().members(userKey);

        if (tokens != null) {
            for (String token : tokens) {
                redisTemplate.delete(TOKEN_PREFIX + token);
            }
        }

        redisStringTemplate.delete(userKey);
    }
}
