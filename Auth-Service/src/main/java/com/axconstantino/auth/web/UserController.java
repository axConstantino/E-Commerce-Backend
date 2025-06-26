package com.axconstantino.auth.web;

import com.axconstantino.auth.application.command.ChangeEmailCommand;
import com.axconstantino.auth.application.command.ChangePasswordCommand;
import com.axconstantino.auth.application.command.ChangeUserNameCommand;
import com.axconstantino.auth.application.usecase.ChangeEmail;
import com.axconstantino.auth.application.usecase.ChangePassword;
import com.axconstantino.auth.application.usecase.ChangeUserName;
import com.axconstantino.auth.application.usecase.DeleteUser;
import com.axconstantino.auth.web.dto.ChangeEmailRequest;
import com.axconstantino.auth.web.dto.ChangePasswordRequest;
import com.axconstantino.auth.web.dto.ChangeUserNameRequest;
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
public class UserController {
    private final ChangePassword changePassword;
    private final ChangeEmail changeEmail;
    private final ChangeUserName changeUserName;
    private final DeleteUser deleteUser;

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt principal,
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

    @PatchMapping("/email")
    public ResponseEntity<Void> changeEmail(@AuthenticationPrincipal Jwt principal,
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

    @PatchMapping("/username")
    public ResponseEntity<Void> changeUsername(@AuthenticationPrincipal Jwt principal,
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

    @DeleteMapping("/delete-account")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal Jwt principal) {
        UUID userId = UUID.fromString(principal.getSubject());
        deleteUser.execute(userId);
        return ResponseEntity.noContent().build();
    }
}
