package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.RegisterUser;
import com.axconstantino.auth.domain.model.Role;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUser {
    private final JwtProvider jwtProvider;
    private final UserRepository repository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;


    /**
     * Registers a new user and generates authentication tokens
     *
     * <p>This method performs the following steps:
     * 1. Validates email uniqueness
     * 2. Creates a new user with default roles
     * 3. Generates access and refresh tokens
     * 4. Captures client context (IP and user agent)
     * 5. Persists user and tokens
     * 6. Caches token for quick validation
     *
     * @param command Contains registration credentials (email and password)
     * @param httpRequest HTTP request object to extract client context
     * @return TokenResponse containing both access and refresh tokens
     * @throws IllegalStateException if email is already registered
     */
    @Override
    @Transactional
    public TokenResponse execute(AuthenticateCommand command, HttpServletRequest httpRequest) {
        log.info("Registering new user with email: {}", command.email());

        validateEmailNotInUse(command.email());

        User user = User.register(
                command.email(),
                encodePassword(command.password()),
                getDefaultRoles()
        );

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        log.debug("Captured client context - IP: {}, User-Agent: {}", ipAddress, userAgent);

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
        log.debug("Generated tokens for user ID: {}", user.getId());

        user.addToken(accessToken);
        user.addToken(refreshToken);

        repository.save(user);
        tokenService.saveTokenInCache(user, accessToken);
        log.info("User registered successfully. ID: {}, Email: {}", user.getId(), user.getEmail());

        return new TokenResponse(accessToken.getToken(), refreshToken.getToken());
    }

    /**
     * Validates email uniqueness in the system
     *
     * @param email Email address to validate
     * @throws IllegalStateException if email is already registered
     */
    private void validateEmailNotInUse(String email) {
        if (repository.existsByEmail(email)) {
            String errorMessage = String.format("Email '%s' is already registered", email);
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Securely encodes raw password using BCrypt
     *
     * @param rawPassword Unencoded password string
     * @return BCrypt-encoded password
     */
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Provides default roles for new users
     *
     * <p>New users receive ROLE_USER by default.
     * Additional roles can be added through admin operations.
     *
     * @return Set containing default role
     */
    private Set<Role> getDefaultRoles() {
        return new HashSet<>(Collections.singleton(Role.ROLE_USER));
    }

}
