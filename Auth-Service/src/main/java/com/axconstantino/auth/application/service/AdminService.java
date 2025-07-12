package com.axconstantino.auth.application.service;

import com.axconstantino.auth.application.usecase.AdminUserManagement;
import com.axconstantino.auth.domain.exception.UserNotFoundException;
import com.axconstantino.auth.domain.model.Role;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService implements AdminUserManagement {
    private final UserRepository repository;

    @Override
    public Page<User> listAllUsers(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public User getUserDetails(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public void deactivateUser(UUID userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.deactivate();
        repository.save(user);
        log.info("User with ID {} has been deactivated", userId);
    }

    @Override
    public void activateUser(UUID userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.activate();
        repository.save(user);
        log.info("User with ID {} has been activated", userId);
    }

    @Override
    public void assignRoleToUser(UUID userId, Role roleName) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.assignRole(roleName);
        repository.save(user);
        log.info("Role {} has been assigned to user with ID {}", roleName, userId);
    }

    @Override
    public void removeRoleFromUser(UUID userId, Role roleName){
        User user = repository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.removeRole(roleName);
        repository.save(user);
        log.info("Role {} has been removed from user with ID {}", roleName, userId);
    }
}
