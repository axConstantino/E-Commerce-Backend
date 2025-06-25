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
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for creating, caching, and revoking authentication tokens.
 * <p>
 * This service provides methods to:
 * <ul>
 *     <li>Create {@link Token} entities from JWT strings.</li>
 *     <li>Store token metadata in Redis cache with automatic expiration (TTL).</li>
 *     <li>Revoke all active tokens for a given user, both in the database and cache.</li>
 * </ul>
 * </p>
 *
 * <p>Tokens include both ACCESS and REFRESH types and are associated with user metadata
 * such as IP address and user agent for additional security tracking.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;
    private final TokenCacheRepository cacheRepository;

    /**
     * Creates a {@link Token} entity using the provided JWT string and user metadata.
     * Extracts issued/expiration timestamps from the JWT and includes request info.
     *
     * @param tokenString The raw JWT string.
     * @param type        The type of token (e.g., ACCESS_TOKEN or REFRESH_TOKEN).
     * @param user        The authenticated user.
     * @param ipAddress   The IP address from which the token was issued.
     * @param userAgent   The client's User-Agent string.
     * @return A fully initialized {@link Token} entity.
     */
    public Token createToken(String tokenString, TokenType type, User user, String ipAddress, String userAgent) {
        Instant issuedAt = jwtProvider.extractIssuedAt(tokenString).toInstant();
        Instant expiresAt = jwtProvider.extractExpiration(tokenString).toInstant();

        log.trace("[TokenService] Creating {} token. Issued at: {}, Expires at: {}", type, issuedAt, expiresAt);

        return new Token(
                tokenString,
                type,
                issuedAt,
                expiresAt,
                ipAddress,
                userAgent,
                true,
                user
        );
    }

    /**
     * Stores a token and its associated metadata in Redis cache with a TTL matching the token's expiration.
     *
     * @param user  The owner of the token.
     * @param token The {@link Token} to be cached.
     */
    public void saveTokenInCache(User user, Token token) {
        Duration ttl = Duration.between(Instant.now(), token.getExpiresAt());

        TokenData data = new TokenData(
                user.getId().toString(),
                token.getToken(),
                true,
                token.getExpiresAt(),
                token.getIpAddress(),
                token.getUserAgent()
        );

        cacheRepository.save(data, ttl.toSeconds(), TimeUnit.SECONDS);

        log.debug("[TokenService] Cached token for user ID: {} with TTL: {} seconds", user.getId(), ttl.getSeconds());
    }

    /**
     * Revokes all valid (non-expired and not previously revoked) tokens for a given user.
     * The tokens are revoked in both the database and the Redis cache.
     *
     * @param user The user whose tokens will be revoked.
     */
    public void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());

        if (validUserTokens.isEmpty()) {
            log.debug("[TokenService] No valid tokens to revoke for user ID: {}", user.getId());
            return;
        }

        log.info("[TokenService] Revoking {} valid tokens for user ID: {}", validUserTokens.size(), user.getId());

        validUserTokens.forEach(token -> {
            token.revoke();
            cacheRepository.delete(token.getToken());
            log.trace("[TokenService] Revoked token with user: {}", token.getUser());
        });

        tokenRepository.saveAll(validUserTokens);
        log.debug("[TokenService] Revoked tokens saved in database for user ID: {}", user.getId());
    }
}