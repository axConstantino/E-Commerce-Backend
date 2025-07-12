package com.axconstantino.auth.web;

import com.axconstantino.auth.application.service.AdminService;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.web.dto.UpdateRoleRequest;
import com.axconstantino.auth.web.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> listAllUsers(
            @PageableDefault(page = 0, size = 20, sort = "userName") Pageable pageable
    ) {
        Page<User> users = adminService.listAllUsers(pageable);
        Page<UserResponse> response = users.map(this::toUserResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserDetails(@PathVariable("id") UUID userId) {
        User user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable("id") UUID userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable("id") UUID userId) {
        adminService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoleToUser(
            @PathVariable("id") UUID userId,
            @RequestBody @Valid UpdateRoleRequest request) {
        adminService.assignRoleToUser(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/roles")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable("id") UUID userId,
            @RequestBody @Valid UpdateRoleRequest request) {
        adminService.removeRoleFromUser(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getRoles(),
                user.isActive(),
                user.isEmailVerified()
        );
    }
}
