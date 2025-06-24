package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.LoginUser;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for authenticating a user and issuing new access and refresh tokens.
 * <p>
 * This process includes:
 * <ul>
 *     <li>Validating user credentials (email/password)</li>
 *     <li>Revoking previously issued tokens</li>
 *     <li>Generating and associating new JWT tokens</li>
 *     <li>Caching tokens for quick validation</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserService implements LoginUser {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user with email and password, generates new JWT tokens and caches them.
     *
     * @param command      Contains email and password.
     * @param httpRequest  Used to extract client metadata (IP, User-Agent).
     * @return TokenResponse containing new access and refresh tokens.
     * @throws BadCredentialsException if email or password are invalid.
     */
    @Override
    @Transactional
    public TokenResponse execute(AuthenticateCommand command, HttpServletRequest httpRequest) {
        log.info("[LoginUserService] Attempting to authenticate user with email: {}", command.email());

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> {
                    log.warn("[LoginUserService] Authentication failed: User not found for email: {}", command.email());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            log.warn("[LoginUserService] Authentication failed: Invalid password for user ID: {}", user.getId());
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("[LoginUserService] User authenticated successfully - ID: {}", user.getId());

        // Revoke old tokens before issuing new ones
        tokenService.revokeAllUserTokens(user);

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        // Generate tokens
        Token accessToken = tokenService.createToken(
                jwtProvider.generateAccessToken(user),
                TokenType.ACCESS_TOKEN,
                user,
                ipAddress,
                userAgent
        );

        Token refreshToken = tokenService.createToken(
                jwtProvider.generateRefreshToken(user),
                TokenType.REFRESH_TOKEN,
                user,
                ipAddress,
                userAgent
        );

        // Associate and persist
        user.addToken(accessToken);
        user.addToken(refreshToken);

        userRepository.save(user);

        // Cache tokens
        tokenService.saveTokenInCache(user, accessToken);
        tokenService.saveTokenInCache(user, refreshToken);

        log.info("[LoginUserService] New tokens generated and saved for user ID: {}", user.getId());

        return new TokenResponse(accessToken.getToken(), refreshToken.getToken());
    }
}