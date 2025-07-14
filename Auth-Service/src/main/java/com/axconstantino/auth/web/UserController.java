package com.axconstantino.auth.web;

import com.axconstantino.auth.application.command.ChangeEmailCommand;
import com.axconstantino.auth.application.command.ChangePasswordCommand;
import com.axconstantino.auth.application.command.ChangeUserNameCommand;
import com.axconstantino.auth.application.usecase.ChangeEmail;
import com.axconstantino.auth.application.usecase.ChangePassword;
import com.axconstantino.auth.application.usecase.ChangeUserName;
import com.axconstantino.auth.application.usecase.DeleteAccount;
import com.axconstantino.auth.web.dto.ChangeEmailRequest;
import com.axconstantino.auth.web.dto.ChangePasswordRequest;
import com.axconstantino.auth.web.dto.ChangeUserNameRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Authenticated user operations (profile, password, account management)")
public class UserController {

    private final ChangePassword changePassword;
    private final ChangeEmail changeEmail;
    private final ChangeUserName changeUserName;
    private final DeleteAccount deleteUser;

    @Operation(
            summary = "Change user password",
            description = "Allows the authenticated user to update their password by providing the current and new password.",
            requestBody = @RequestBody(
                    description = "Current and new password",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid current password")
            }
    )
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt principal,
            @RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        UUID userId = UUID.fromString(principal.getSubject());

        ChangePasswordCommand command = new ChangePasswordCommand(
                userId,
                changePasswordRequest.getCurrentPassword(),
                changePasswordRequest.getNewPassword()
        );

        changePassword.execute(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Change user email",
            description = "Allows the authenticated user to change their email address by confirming their current password.",
            requestBody = @RequestBody(
                    description = "Current password and new email address",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangeEmailRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Email updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid current password or email already in use")
            }
    )
    @PatchMapping("/email")
    public ResponseEntity<Void> changeEmail(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt principal,
            @RequestBody @Valid ChangeEmailRequest changeEmailRequest) {
        UUID userId = UUID.fromString(principal.getSubject());

        ChangeEmailCommand command = new ChangeEmailCommand(
                userId,
                changeEmailRequest.getCurrentPassword(),
                changeEmailRequest.getNewEmail()
        );

        changeEmail.execute(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Change username",
            description = "Allows the authenticated user to change their username by confirming their current password.",
            requestBody = @RequestBody(
                    description = "Current password and new username",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangeUserNameRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Username updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid current password or username already taken")
            }
    )
    @PatchMapping("/username")
    public ResponseEntity<Void> changeUsername(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt principal,
            @RequestBody @Valid ChangeUserNameRequest changeUserNameRequest) {
        UUID userId = UUID.fromString(principal.getSubject());

        ChangeUserNameCommand command = new ChangeUserNameCommand(
                userId,
                changeUserNameRequest.getCurrentPassword(),
                changeUserNameRequest.getNewUsername()
        );

        changeUserName.execute(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Soft delete account",
            description = "Marks the authenticated user's account as deleted (soft delete). " +
                    "The account is deactivated but not permanently removed from the database.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Account soft-deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        deleteUser.execute(userId);
        return ResponseEntity.noContent().build();
    }

}
