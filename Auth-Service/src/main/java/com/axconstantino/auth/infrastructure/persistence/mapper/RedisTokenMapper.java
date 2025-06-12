package com.axconstantino.auth.infrastructure.persistence.mapper;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.infrastructure.redis.model.RedisTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RedisTokenMapper {
    RedisTokenEntity toEntity(Token token);
    Token toDomain(RedisTokenEntity entity);
}
