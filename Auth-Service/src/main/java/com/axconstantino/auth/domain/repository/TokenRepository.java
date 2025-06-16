package com.axconstantino.auth.domain.repository;

import com.axconstantino.auth.domain.model.Token;

import java.util.List;
import java.util.UUID;

public interface TokenRepository {
    List<Token> findAllByUserId(UUID userId);
    List<Token> findAllValidTokensByUser(UUID userId);
    void saveAll(List<Token> tokens);
    void save(Token token);
}
