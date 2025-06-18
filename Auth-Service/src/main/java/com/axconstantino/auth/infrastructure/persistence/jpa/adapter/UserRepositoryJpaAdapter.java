package com.axconstantino.auth.infrastructure.persistence.jpa.adapter;

import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
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
    public void save(User user) {
        jpaRepo.save(mapper.toEntity(user));
    }
}
