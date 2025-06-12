package com.axconstantino.auth.infrastructure.persistence.mapper;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.infrastructure.persistence.jpa.entity.TokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TokenJpaMapper {
    Token toDomain(TokenEntity entity);
    TokenEntity toEntity(Token token);
}
