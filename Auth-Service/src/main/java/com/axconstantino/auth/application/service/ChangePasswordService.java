package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.ChangePasswordCommand;
import com.axconstantino.auth.application.usecase.ChangePassword;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.domain.exception.UserNotFoudException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to handle updating the password of an authenticated user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePassword {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates the current password and updates it with the new one.
     *
     * @param command Contains userId, current password, and new password.
     */
    @Override
    @Transactional
    public void execute(ChangePasswordCommand command) {
        User user = repository.findById(command.userId())
                .orElseThrow(() -> {
                    log.error("[ChangePasswordService] User not found with ID: {}", command.userId());
                    return new UserNotFoudException("User not found");
                });

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            log.warn("[ChangePasswordService] Incorrect current password for user ID: {}", command.userId());
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        log.info("[ChangePasswordService] Password updated for user ID: {}", user.getId());
    }
}

