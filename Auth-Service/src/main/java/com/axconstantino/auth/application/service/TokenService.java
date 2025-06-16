package com.axconstantino.auth.application.service;

import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.TokenCacheRepository;
import com.axconstantino.auth.domain.repository.TokenRepository;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import com.axconstantino.auth.infrastructure.redis.model.TokenData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;
    private final TokenCacheRepository cacheRepository;

    /**
     * Creates a Token entity from a JWT string and metadata.
     *
     * @param tokenString The JWT in string format.
     * @param type The token type (ACCESS_TOKEN or REFRESH_TOKEN).
     * @param ipAddress The client's IP address.
     * @param userAgent The client's User-Agent.
     * @return A new Token entity, ready to be associated with a user.
     */
    public Token createToken(String tokenString, TokenType  type, String ipAddress, String userAgent) {
        Instant issuedAt = jwtProvider.extractIssuedAt(tokenString).toInstant();
        Instant expiresAt = jwtProvider.extractExpiration(tokenString).toInstant();
        log.trace("Created {} token. Issued: {}, Expires: {}", type, issuedAt, expiresAt);
        return new Token(tokenString, type, issuedAt, expiresAt, ipAddress, userAgent, true);
    }

    /**
     * Guarda la información de un token en el caché (Redis) con un TTL apropiado.
     *
     * @param user El usuario dueño del token.
     * @param token El token a cachear.
     */
    public void saveTokenInCache(User user, Token token) {
        TokenData data = new TokenData(
                user.getId().toString(),
                true,
                token.getExpiresAt(),
                token.getIpAddress(),
                token.getUserAgent()
        );

        Duration ttl = Duration.between(Instant.now(), token.getExpiresAt());
        cacheRepository.save(token.getToken(), data, ttl);
        log.debug("Cached token for user ID: {} with TTL: {} seconds", user.getId(), ttl.getSeconds());
    }

    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }

        log.debug("Revoking {} old tokens for user ID: {}", validUserTokens.size(), user.getId());
        validUserTokens.forEach(token -> {
            token.revoke();
            cacheRepository.delete(token.getToken());
        });
        tokenRepository.saveAll(validUserTokens);
    }

}
