package com.axconstantino.auth.web;

import com.axconstantino.auth.application.service.AdminService;
import com.axconstantino.auth.domain.model.User;
import com.axconstantino.auth.web.dto.UpdateRoleRequest;
import com.axconstantino.auth.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PreAuthorize("hasAuthority('ADMIN')")
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Admin operations for managing users")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "List all users",
            description = "Returns a paginated list of all registered users.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listAllUsers(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(page = 0, size = 20, sort = "userName") Pageable pageable
    ) {
        Page<User> users = adminService.listAllUsers(pageable);
        Page<UserResponse> response = users.map(this::toUserResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get user details",
            description = "Retrieves detailed information about a user by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserDetails(
            @Parameter(description = "User ID") @PathVariable("id") UUID userId) {
        User user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(toUserResponse(user));
    }

    @Operation(
            summary = "Deactivate user (soft delete)",
            description = "Marks the user as inactive without permanently deleting their account.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deactivated (soft deleted) successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "User ID") @PathVariable("id") UUID userId) {
        adminService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Activate user",
            description = "Marks a user as active.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User activated successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "User ID") @PathVariable("id") UUID userId) {
        adminService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Assign role to user",
            description = "Assigns a new role to an existing user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role to assign",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateRoleRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Role assigned successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoleToUser(
            @Parameter(description = "User ID") @PathVariable("id") UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        adminService.assignRoleToUser(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove role from user",
            description = "Removes a role previously assigned to a user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role to remove",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateRoleRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Role removed successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{id}/roles")
    public ResponseEntity<Void> removeRoleFromUser(
            @Parameter(description = "User ID") @PathVariable("id") UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {
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