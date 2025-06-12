package com.axconstantino.auth.domain.repository;

import com.axconstantino.auth.domain.model.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TokenRepository {
    Optional<Token> findByToken(String token);
    List<Token> findAllByUserId(UUID userId);
    void save(Token token);
    void delete(String token);
    void deleteAllByUserId(UUID userId);
}
