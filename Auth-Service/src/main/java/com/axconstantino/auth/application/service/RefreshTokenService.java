package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.RefreshToken;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.repository.TokenRepository;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for refreshing authentication tokens using a valid refresh token.
 * <p>
 * This process includes:
 * <ul>
 *     <li>Validating the refresh token</li>
 *     <li>Revoking the old refresh token</li>
 *     <li>Generating new access and refresh tokens</li>
 *     <li>Storing and caching the new tokens</li>
 * </ul>
 * </p>
 * <p>
 * This service ensures that refresh tokens cannot be reused, improving security.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshToken {

    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;

    /**
     * Executes the token refresh flow based on the provided refresh token from the Authorization header.
     *
     * @param httpRequest HTTP request containing the Bearer refresh token and client context.
     * @return TokenResponse with new access and refresh tokens.
     * @throws BadCredentialsException if the token is invalid, expired, or reused.
     */
    @Override
    @Transactional
    public TokenResponse execute(HttpServletRequest httpRequest) {
        final String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        // Extract token from header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[RefreshTokenService] Authorization header missing or malformed");
            throw new BadCredentialsException("Refresh token is missing or invalid");
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtProvider.extractUsername(refreshToken);

        if (userEmail == null) {
            log.warn("[RefreshTokenService] Token does not contain a valid user email");
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Validate token in database
        var storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("[RefreshTokenService] Refresh token not found in DB. Token: {}", refreshToken);
                    return new BadCredentialsException("Invalid refresh token");
                });

        if (!storedToken.isValid() || storedToken.getTokenType() != TokenType.REFRESH_TOKEN) {
            log.warn("[RefreshTokenService] Token is invalid or not a refresh token. User: {}", userEmail);
            throw new BadCredentialsException("Invalid refresh token");
        }

        if (jwtProvider.isTokenExpired(refreshToken)) {
            log.warn("[RefreshTokenService] Token expired for user: {}", userEmail);
            throw new BadCredentialsException("Refresh token has expired");
        }

        // Generate new tokens
        var user = storedToken.getUser();
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        // Revoke old token
        storedToken.revoke();
        tokenRepository.save(storedToken);
        log.debug("[RefreshTokenService] Revoked old refresh token for user: {}", userEmail);

        // Capture client metadata
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        // Create and store new tokens
        Token newAccessTokenEntity = tokenService.createToken(newAccessToken, TokenType.ACCESS_TOKEN, user, ipAddress, userAgent);
        Token newRefreshTokenEntity = tokenService.createToken(newRefreshToken, TokenType.REFRESH_TOKEN, user, ipAddress, userAgent);

        user.addToken(newAccessTokenEntity);
        user.addToken(newRefreshTokenEntity);

        tokenService.saveTokenInCache(user, newAccessTokenEntity);
        log.info("[RefreshTokenService] Tokens refreshed successfully for user: {}", userEmail);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}