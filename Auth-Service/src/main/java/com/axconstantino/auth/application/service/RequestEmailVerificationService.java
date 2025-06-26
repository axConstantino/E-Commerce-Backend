package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.usecase.RequestEmailVerification;
import com.axconstantino.auth.domain.event.EmailVerificationEvent;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.jwt.JwtProvider;
import com.axconstantino.auth.infrastructure.kafka.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating and sending an email verification link to a user.
 * <p>
 * This class handles:
 * <ul>
 * <li>User lookup by email.</li>
 * <li>JWT generation for email verification.</li>
 * <li>Temporary token storage in Redis.</li>
 * <li>Publishing an event to trigger email delivery.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestEmailVerificationService implements RequestEmailVerification {

    private final JwtProvider jwtProvider;
    private final UserRepository repository;
    private final EventPublisherService publisherService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${verification.email.expiration-millis}")
    private long emailVerificationExpirationMillis;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    /**
     * Executes the flow to send a verification email to the given address.
     *
     * @param email the email address of the user to verify
     * @throws UserNotFoundException if no user is found with the provided email
     */
    @Override
    public void execute(String email) {
        log.info("Starting email verification request for: {}", email);

        // Step 1: Retrieve user by email
        User user = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UserNotFoundException("User not found");
                });

        // Step 2: Generate verification token
        String token = jwtProvider.generateEmailVerificationToken(
                email,
                user.getId(),
                emailVerificationExpirationMillis
        );
        log.debug("Generated email verification token for user ID {}: {}", user.getId(), token);

        // Step 3: Store token in Redis with expiration
        String redisKey = String.format("email-verification:%s:%s", user.getId(), token);
        redisTemplate.opsForValue().set(redisKey, token, emailVerificationExpirationMillis);
        log.debug("Stored token in Redis under key: {}", redisKey);

        // Step 4: Construct verification link
        String verificationLink = String.format("%s/verify-email?token=%s", frontendBaseUrl, token);
        log.info("Constructed email verification link for user {}: {}", user.getEmail(), verificationLink);

        // Step 5: Publish email verification event
        EmailVerificationEvent event = new EmailVerificationEvent(email, verificationLink);
        publisherService.publishEmailVerificationEvent(event);
        log.info("Published email verification event for: {}", email);
    }
}
