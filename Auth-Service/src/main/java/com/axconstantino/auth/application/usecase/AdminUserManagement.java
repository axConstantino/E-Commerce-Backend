package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.domain.model.Role;
import com.axconstantino.auth.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface AdminUserManagement {
    Page<User> listAllUsers(Pageable pageable);
    User getUserDetails(UUID userId);
    void deactivateUser(UUID userId);
    void activateUser(UUID userId);
    void assignRoleToUser(UUID userId, Role roleName);
    void removeRoleFromUser(UUID userId, Role roleName);
}
