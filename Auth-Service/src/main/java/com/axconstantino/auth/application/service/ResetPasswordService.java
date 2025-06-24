package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.ResetPasswordCommand;
import com.axconstantino.auth.application.usecase.ResetPassword;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.domain.exception.UserNotFoudException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that handles the logic to reset a user's password after verifying the code sent to their email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordService implements ResetPassword {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Verifies the reset code and updates the user's password if valid.
     *
     * @param command Contains email, code, and the new password.
     */
    @Override
    @Transactional
    public void execute(ResetPasswordCommand command) {
        User user = repository.findByEmail(command.email())
                .orElseThrow(() -> new UserNotFoudException("User not found"));

        String key = "password-reset" + user.getId();
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null || !storedCode.equals(command.code())) {
            log.warn("[ResetPasswordService] Invalid or expired code for user: {}", command.email());
            throw new BadCredentialsException("Invalid or expired code");
        }

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        redisTemplate.delete(key);
        log.info("[ResetPasswordService] Password successfully reset for user ID: {}", user.getId());
    }
}
