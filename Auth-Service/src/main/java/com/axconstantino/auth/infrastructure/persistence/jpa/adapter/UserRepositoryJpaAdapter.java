package com.axconstantino.auth.infrastructure.persistence.jpa.adapter;

import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.persistence.jpa.entity.UserEntity;
import com.axconstantino.auth.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.axconstantino.auth.infrastructure.persistence.mapper.UserJpaMapper;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserRepositoryJpaAdapter implements UserRepository {

    private final UserJpaRepository jpaRepo;
    private final UserJpaMapper mapper;

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepo.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepo.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepo.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = mapper.toEntity(user);
        UserEntity savedUser = jpaRepo.save(userEntity);
        return mapper.toDomain(savedUser);
    }

    @Override
    public void deleteById(UUID id) {
        UserEntity user = jpaRepo.findById(id)
                .orElseThrow();

        User deletedUser = mapper.toDomain(user);
        deletedUser.deactivate();
    }
}
