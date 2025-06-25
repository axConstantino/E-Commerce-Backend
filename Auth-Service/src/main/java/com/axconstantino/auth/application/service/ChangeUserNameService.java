package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.command.ChangeUserNameCommand;
import com.axconstantino.auth.application.usecase.ChangeUserName;
import com.axconstantino.auth.domain.exception.BadCredentialsException;
import com.axconstantino.auth.domain.exception.DuplicateCredentialsException;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service to handle changing the username of an authenticated user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeUserNameService implements ChangeUserName {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Changes the username of a user after verifying credentials and uniqueness.
     *
     * @param command Contains userId, current password, and new username.
     */
    @Override
    public void execute(ChangeUserNameCommand command) {
        User user = repository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            log.warn("[ChangeUserNameService] Incorrect password for user ID: {}", command.userId());
            throw new BadCredentialsException("Incorrect Password");
        }

        if (repository.existsByUserName(command.newName())) {
            log.warn("[ChangeUserNameService] Username already in use: {}", command.newName());
            throw new DuplicateCredentialsException("The username is already in use");
        }

        user.changeName(command.newName());
        log.info("[ChangeUserNameService] Username updated for user ID: {}", user.getId());
        tokenService.revokeAllUserTokens(user);
        log.info("[ChangeEmailService] All tokens revoked for user ID: {}", user.getId());
    }
}
