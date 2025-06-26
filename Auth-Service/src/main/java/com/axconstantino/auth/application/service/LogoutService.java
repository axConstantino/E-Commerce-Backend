package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.usecase.Logout;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.domain.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for handling user logout by revoking the access token.
 * <p>
 * The logout process includes:
 * <ul>
 *     <li>Extracting the Bearer token from the Authorization header.</li>
 *     <li>Validating and locating the token in the database.</li>
 *     <li>Revoking the token and marking it as invalid.</li>
 *     <li>Removing the token from Redis cache to prevent reuse.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This service is transactional to ensure the token revocation is committed to the database.
 * </p>
 *
 * <p>
 * If the Authorization header is missing, malformed, or the token is not found in the system,
 * a {@link BadCredentialsException} is thrown.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements Logout {
    private final TokenRepository tokenRepository;
    private final TokenService tokenService;


    /**
     * Executes the logout operation by revoking the current access token.
     *
     * @param request The HTTP request containing the Bearer token in the Authorization header.
     * @throws BadCredentialsException if the token is missing, malformed, or not found in the system.
     */
    @Override
    @Transactional
    public void execute(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[LogoutService] Missing or invalid Authorization header");
            throw new BadCredentialsException("Invalid token");
        }

        String token = authHeader.substring(7);

        tokenRepository.findByToken(token).ifPresentOrElse(storedToken -> {
            storedToken.revoke();
            tokenRepository.save(storedToken);
            tokenService.deleteTokenFromCache(token);
            log.info("[LogoutService] Token successfully revoked");
        }, () -> {
            log.warn("[LogoutService] Token not found in DB: {}", token);
            throw new BadCredentialsException("Invalid token");
        });
    }
}
