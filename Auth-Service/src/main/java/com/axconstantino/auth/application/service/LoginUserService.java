package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.LoginUser;
import com.axconstantino.auth.domain.exception.GlobalExceptionHandler;
import com.axconstantino.auth.domain.exception.EmailNotVerifiedException;
import com.axconstantino.auth.domain.exception.InactiveUserException;
import com.axconstantino.auth.domain.exception.LockedException;
import com.axconstantino.auth.domain.model.Token;
import com.axconstantino.auth.domain.model.TokenType;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Service responsible for authenticating a user and issuing new JWT access and refresh tokens.
 * <p>
 * This includes:
 * <ul>
 *     <li>Verifying the user's credentials (email and password)</li>
 *     <li>Rejecting access if the user is inactive or has not verified their email</li>
 *     <li>Enforcing brute-force protection with Redis (max 5 failed attempts in 15 minutes)</li>
 *     <li>Revoking all previously issued tokens</li>
 *     <li>Generating and associating new signed JWTs</li>
 *     <li>Caching tokens in Redis for fast validation by the API Gateway</li>
 * </ul>
 * </p>
 * <p>
 * If authentication fails due to invalid credentials or exceeded attempt limits,
 * an appropriate exception is thrown and handled globally by {@link GlobalExceptionHandler}.
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
    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_EXPIRATION = Duration.ofMinutes(15);

    /**
     * Authenticates a user by verifying their credentials and issuing new tokens.
     *
     * @param command      Login command containing email and password.
     * @param request      The incoming HTTP request, used to extract IP and user-agent.
     * @return TokenResponse containing new JWT access and refresh tokens.
     * @throws LockedException              if login attempts exceded the allowed threshold.
     * @throws EmailNotVerifiedException    if the user's email is not verified.
     * @throws InactiveUserException        if the user's account is inactive.
     * @throws BadCredentialsException      if the credentials are invalid.
     */
    @Override
    @Transactional
    public TokenResponse execute(AuthenticateCommand command, HttpServletRequest request) {
        String email = command.email().toLowerCase();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        log.info("[Login] Attempting login for email: {}", email);
        checkLoginAttempts(email);

        User user = validateUserCredentials(command);

        resetLoginAttempts(email);
        revokeOldTokens(user);

        TokenResponse response = generateAndSaveTokens(user, ip, userAgent);
        log.info("[Login] Authentication successful for user ID: {}", user.getId());

        return response;
    }

    // --- Validation and security checks ---

    private void checkLoginAttempts(String email) {
        String key = "login:fail:" + email;
        String attemptsStr = redisTemplate.opsForValue().get(key);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("[Login] Too many failed attempts for email: {}. Blocking login.", email);
            throw new LockedException("Too many failed login attempts. Try again in 15 minutes.");
        }
    }

    private void registerFailedAttempt(String email) {
        String key = "login:fail:" + email;
        Long attempts = redisTemplate.opsForValue().increment(key);
        if (attempts == 1) {
            redisTemplate.expire(key, ATTEMPT_EXPIRATION);
        }
        log.warn("[Login] Failed login attempt #{} for email: {}", attempts, email);
    }

    private void resetLoginAttempts(String email) {
        redisTemplate.delete("login:fail:" + email);
        log.debug("[Login] Login attempts reset for email: {}", email);
    }

    private User validateUserCredentials(AuthenticateCommand command) {
        return userRepository.findByEmail(command.email())
                .filter(user -> {
                    if (!user.isEmailVerified()) {
                        log.warn("[Login] Email not verified for user: {}", user.getEmail());
                        throw new EmailNotVerifiedException("Email not verified");
                    }
                    if (!user.isActive()) {
                        log.warn("[Login] User is inactive: {}", user.getEmail());
                        throw new InactiveUserException("User is inactive");
                    }
                    return true;
                })
                .filter(user -> {
                    if (!passwordEncoder.matches(command.password(), user.getPassword())) {
                        registerFailedAttempt(command.email());
                        throw new BadCredentialsException("Invalid email or password");
                    }
                    return true;
                })
                .orElseThrow(() -> {
                    registerFailedAttempt(command.email());
                    log.warn("[Login] Email not found: {}", command.email());
                    return new BadCredentialsException("Invalid email or password");
                });
    }

    // --- Token lifecycle ---

    private void revokeOldTokens(User user) {
        log.debug("[Login] Revoking previous tokens for user ID: {}", user.getId());
        tokenService.revokeAllUserTokens(user);
    }

    private TokenResponse generateAndSaveTokens(User user, String ip, String userAgent) {
        log.debug("[Login] Generating tokens for user ID: {}", user.getId());

        Token accessToken = tokenService.createToken(
                jwtProvider.generateAccessToken(user),
                TokenType.ACCESS_TOKEN,
                user, ip, userAgent
        );

        Token refreshToken = tokenService.createToken(
                jwtProvider.generateRefreshToken(user),
                TokenType.REFRESH_TOKEN,
                user, ip, userAgent
        );

        user.addToken(accessToken);
        user.addToken(refreshToken);
        userRepository.save(user);

        tokenService.saveTokenInCache(user, accessToken);
        tokenService.saveTokenInCache(user, refreshToken);

        log.debug("[Login] Tokens generated and persisted for user ID: {}", user.getId());

        return new TokenResponse(accessToken.getToken(), refreshToken.getToken());
    }
}
