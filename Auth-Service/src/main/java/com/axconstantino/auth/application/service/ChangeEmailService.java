package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.ChangeEmailCommand;
import com.axconstantino.auth.application.usecase.ChangeEmail;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.domain.exception.DuplicateCredentialsException;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to handle changing the email of an authenticated user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeEmailService implements ChangeEmail {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Changes the user's email after verifying password and uniqueness of new email.
     *
     * @param command Contains userId, current password, and new email.
     */
    @Override
    @Transactional
    public void execute(ChangeEmailCommand command) {
        User user = repository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            log.warn("[ChangeEmailService] Incorrect password for user ID: {}", command.userId());
            throw new BadCredentialsException("Incorrect Password.");
        }

        if (repository.existsByEmail(command.newEmail())) {
            log.warn("[ChangeEmailService] Email already in use: {}", command.newEmail());
            throw new DuplicateCredentialsException("The email is already in use");
        }

        user.changeEmail(command.newEmail());
        log.info("[ChangeEmailService] Email updated for user ID: {}", user.getId());
        tokenService.revokeAllUserTokens(user);
        log.info("[ChangeEmailService] All tokens revoked for user ID: {}", user.getId());
    }
}
