package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.usecase.VerifyEmail;
import com.axconstantino.auth.domain.exception.InvalidOrExpiredTokenException;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for verifying the user's email based on a token.
 *
 * <p>This service performs the following steps:</p>
 *
 * <ul>
 *   <li>Parses and validates the JWT token.</li>
 *   <li>Checks if the token is still valid and stored in Redis.</li>
 *   <li>Confirms the user exists.</li>
 *   <li>Marks the user's email as verified.</li>
 *   <li>Deletes the token from Redis after successful verification.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailService implements VerifyEmail {

    private final JwtProvider jwtProvider;
    private final UserRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Verifies the user's email using the provided token.
     *
     * @param token JWT token received from the user
     * @throws InvalidOrExpiredTokenException if the token is invalid, expired or already used
     * @throws UserNotFoundException          if the user associated with the token does not exist
     */
    @Override
    @Transactional
    public void execute(String token) {
        log.info("Starting email verification process.");

        // Step 1: Extract and validate token claims
        Claims claims = jwtProvider.extractAllClaims(token);

        if (jwtProvider.isTokenExpired(token)) {
            log.warn("Email verification token has expired.");
            throw new InvalidOrExpiredTokenException("Token expired");
        }

        String type = claims.get("type", String.class);
        if (!"email-verification".equals(type)) {
            log.warn("Invalid token type received: {}", type);
            throw new InvalidOrExpiredTokenException("Invalid token type");
        }

        UUID userId = UUID.fromString(claims.get("userId", String.class));
        String email = claims.getSubject();
        String redisKey = "email-verification:" + userId + ":" + token;

        // Step 2: Check token presence in Redis
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            log.warn("Verification token not found in Redis or already used for userId: {}", userId);
            throw new InvalidOrExpiredTokenException("Token is invalid or already used");
        }

        // Step 3: Retrieve and verify user
        User user = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found");
                });

        user.verifyEmail();
        repository.save(user);
        log.info("Email marked as verified for user ID: {}, email: {}", userId, email);

        // Step 4: Delete token from Redis
        redisTemplate.delete(redisKey);
        log.debug("Deleted email verification token from Redis for user ID: {}", userId);
    }
}
