package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.RefreshToken;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.repository.TokenRepository;
import com.axconstantino.auth.exception.BadCredentialsException;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshToken {
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public TokenResponse execute(HttpServletRequest httpRequest) {
        final String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;

        if (authHeader == null && !authHeader.startsWith("Bearer ")) {
            log.warn("Refresh token attempt failed: Authorization header missing or invalid");
            throw new BadCredentialsException("RefreshToken is missing or invalid");
        }

        refreshToken = authHeader.substring(7);
        userEmail = jwtProvider.extractUsername(refreshToken);

        if (userEmail == null) {
            log.warn("Refresh token attempt failed: Token does not contain a valid user email");
            throw new BadCredentialsException("Invalid refresh token");
        }

        var storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token attempt failed: Token not found in database. Token: {}", refreshToken);
                    return new BadCredentialsException("Invalid refresh token");
                });

        if (!storedToken.isValid() || storedToken.getTokenType() != TokenType.REFRESH_TOKEN) {
            log.warn("Refresh token attempt failed: Token is invalid or not a refresh token. User: {}", userEmail);
            throw new BadCredentialsException("Invalid refresh token.");
        }

        if (jwtProvider.isTokenExpired(refreshToken)) {
            log.warn("Refresh token attempt failed: Token has expired. User: {}", userEmail);
            throw new BadCredentialsException("Refresh token has expired.");
        }

        var user = storedToken.getUser();

        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        storedToken.revoke();
        tokenRepository.save(storedToken);

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        Token newAccessTokenEntity = tokenService.createToken(newAccessToken, TokenType.ACCESS_TOKEN, user, ipAddress, userAgent);
        Token newRefreshTokenEntity = tokenService.createToken(newRefreshToken, TokenType.REFRESH_TOKEN, user, ipAddress, userAgent);

        user.addToken(newAccessTokenEntity);
        user.addToken(newRefreshTokenEntity);

        tokenService.saveTokenInCache(user, newAccessTokenEntity);

        log.info("Tokens successfully refreshed for user: {}", userEmail);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
