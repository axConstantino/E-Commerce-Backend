package com.axconstantino.auth.infrastructure.persistence.jpa.adapter;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.repository.TokenRepository;
import com.axconstantino.auth.infrastructure.persistence.jpa.repository.TokenJpaRepository;
import com.axconstantino.auth.infrastructure.persistence.mapper.TokenJpaMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class TokenRepositoryJpaAdapter implements TokenRepository {

    private final TokenJpaRepository jpaRepo;
    private final TokenJpaMapper mapper;

    @Override
    public Optional<Token> findByToken(String token) {
        return jpaRepo.findByTokenValue(token)
                .map(mapper::toDomain);
    }

    @Override
    public List<Token> findAllValidTokensByUser(UUID userId) {
        return jpaRepo.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Token> tokens) {
        jpaRepo.saveAll(tokens.stream()
                .map(mapper::toEntity)
                .toList());
    }

    @Override
    public void save(Token token) {
        jpaRepo.save(mapper.toEntity(token));
    }

 }
