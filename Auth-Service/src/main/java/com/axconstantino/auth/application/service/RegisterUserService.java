package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.RegisterCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.RegisterUser;
import com.axconstantino.auth.domain.event.UserRegisteredEvent;
import com.axconstantino.auth.domain.exception.DuplicateCredentialsException;
import com.axconstantino.auth.domain.model.Role;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import com.axconstantino.auth.infrastructure.kafka.EventPublisherService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Service responsible for handling new user registrations.
 * <p>
 * The registration process includes:
 * <ul>
 *     <li>Validating email and username uniqueness</li>
 *     <li>Creating and persisting a new {@link User} with default roles</li>
 *     <li>Generating access and refresh JWT tokens</li>
 *     <li>Saving tokens and caching the access token for quick validation</li>
 *     <li>Publishing a {@link UserRegisteredEvent} to Kafka for further processing</li>
 * </ul>
 * </p>
 * <p>
 * This service is transactional, ensuring all steps are committed atomically.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUser {

    private final JwtProvider jwtProvider;
    private final UserRepository repository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisherService eventPublisher;

    /**
     * Registers a new user and generates authentication tokens.
     *
     * @param command      Contains email, password, and username from client.
     * @param httpRequest  Used to extract client context (IP and User-Agent).
     * @return TokenResponse containing both access and refresh tokens.
     * @throws DuplicateCredentialsException if the email or username is already in use.
     */
    @Override
    @Transactional
    public TokenResponse execute(RegisterCommand command, HttpServletRequest httpRequest) {
        log.info("[RegisterUserService] Starting registration process for email: {}", command.email());

        validateEmailNotInUse(command.email());
        validateUserNameNotInUse(command.userName());

        User user = User.register(
                command.userName(),
                command.email(),
                encodePassword(command.password()),
                getDefaultRoles()
        );

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        log.debug("[RegisterUserService] Captured client context - IP: {}, User-Agent: {}", ipAddress, userAgent);

        // Generate and associate tokens
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

        user.addToken(accessToken);
        user.addToken(refreshToken);

        repository.save(user);
        tokenService.saveTokenInCache(user, accessToken);

        log.info("[RegisterUserService] User registered successfully - ID: {}, Email: {}", user.getId(), user.getEmail());

        // Publish registration event to Kafka
        eventPublisher.publishUserRegisteredEvent(new UserRegisteredEvent(
                user.getId(),
                user.getUserName(),
                user.getEmail()
        ));

        return new TokenResponse(accessToken.getToken(), refreshToken.getToken());
    }

    /**
     * Ensures that the provided email is not already associated with an existing user.
     *
     * @param email the email to validate
     * @throws DuplicateCredentialsException if the email is already registered
     */
    private void validateEmailNotInUse(String email) {
        if (repository.existsByEmail(email)) {
            String errorMessage = String.format("Email '%s' is already registered", email);
            log.error("[RegisterUserService] {}", errorMessage);
            throw new DuplicateCredentialsException(errorMessage);
        }
    }

    /**
     * Ensures that the provided username is unique across all users.
     *
     * @param userName the username to validate
     * @throws DuplicateCredentialsException if the username is already taken
     */
    private void validateUserNameNotInUse(String userName) {
        if (repository.existsByUserName(userName)) {
            String errorMessage = String.format("Username '%s' is already in use", userName);
            log.error("[RegisterUserService] {}", errorMessage);
            throw new DuplicateCredentialsException(errorMessage);
        }
    }

    /**
     * Encodes the raw password securely using BCrypt.
     *
     * @param rawPassword the plain-text password
     * @return the encoded password
     */
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Returns the default roles assigned to a new user.
     * By default, all users receive ROLE_USER.
     *
     * @return a set containing the default role(s)
     */
    private Set<Role> getDefaultRoles() {
        return new HashSet<>(Collections.singleton(Role.ROLE_USER));
    }
}

