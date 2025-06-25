package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.usecase.ForgotPassword;
import com.axconstantino.auth.domain.event.PasswordResetEvent;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.kafka.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;

/**
 * Service responsible for initiating the password reset process by generating and sending a code via Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService implements ForgotPassword {

    private final UserRepository repository;
    private final EventPublisherService eventPublisherService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Random RANDOM = new SecureRandom();

    /**
     * Generates a reset code and stores it in Redis, then publishes an event to trigger email notification.
     *
     * @param email The email of the user requesting password reset.
     */
    @Override
    public void execute(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String code = generate6DigitCode();
        String key = "password-reset" + user.getId();

        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));
        eventPublisherService.publishPasswordResetEvent(new PasswordResetEvent(email, code));

        log.info("[ForgotPasswordService] Reset code generated and published for user ID: {}", user.getId());
    }

    private String generate6DigitCode() {
        int code = RANDOM.nextInt(1_000_000);
        return String.format("%06d", code);
    }
}
