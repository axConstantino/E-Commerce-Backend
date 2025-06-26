package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.usecase.DeleteUser;
import com.axconstantino.auth.domain.event.UserDeletedEvent;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import com.axconstantino.auth.infrastructure.kafka.EventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for deleting a user from the system.
 * <p>
 * This service handles the deletion of a user by their ID, revokes all associated tokens,
 * and publishes an event indicating that the user has been deleted.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserService implements DeleteUser {

    private final UserRepository repository;
    private final TokenService tokenService;
    private final EventPublisherService eventPublisherService;

    @Override
    @Transactional
    public void execute(UUID userID) {
        User user = repository.findById(userID)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userID));

        log.info("Deleting user with ID: {}", userID);
        repository.deleteById(userID);
        tokenService.revokeAllUserTokens(user);
        eventPublisherService.publishUserDeletedEvent(new UserDeletedEvent(userID));
    }
}
