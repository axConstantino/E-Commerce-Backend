package com.axconstantino.auth.infrastructure.persistence.mapper;

import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.infrastructure.persistence.jpa.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserJpaMapper {
    User toDomain(UserEntity userEntity);
    UserEntity toEntity(User user);
}
