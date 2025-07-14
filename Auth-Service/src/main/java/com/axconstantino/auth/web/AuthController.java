package com.axconstantino.auth.web;

import com.axconstantino.auth.application.command.*;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.*;
import com.axconstantino.auth.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Handles authentication, registration, and user account actions")
public class AuthController {

    private final LoginUser loginUser;
    private final RegisterUser registerUser;
    private final RefreshToken refreshToken;
    private final ResetPassword resetPassword;
    private final ForgotPassword forgotPassword;
    private final RequestEmailVerification requestEmailVerification;
    private final VerifyEmail verifyEmail;
    private final Logout logout;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns an authentication token.",
            requestBody = @RequestBody(
                    description = "Registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        RegisterCommand command = new RegisterCommand(
                request.getUserName(),
                request.getEmail(),
                request.getPassword()
        );
        final TokenResponse response = registerUser.execute(command, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Log in a user",
            description = "Authenticates the user and returns a JWT token.",
            requestBody = @RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthenticateRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(@Valid @RequestBody AuthenticateRequest request, HttpServletRequest httpRequest) {
        AuthenticateCommand command = new AuthenticateCommand(
                request.getEmail(),
                request.getPassword()
        );
        final TokenResponse response = loginUser.execute(command, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Log out user",
            description = "Revokes the current user's session token.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User logged out successfully")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        logout.execute(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Refresh authentication token",
            description = "Generates a new JWT token using the refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(HttpServletRequest httpRequest) {
        TokenResponse response = refreshToken.execute(httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset code to the user's email.",
            requestBody = @RequestBody(
                    description = "User email",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ForgotPasswordRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Reset email sent")
            }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        forgotPassword.execute(forgotPasswordRequest.getEmail());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using the reset code.",
            requestBody = @RequestBody(
                    description = "Reset password data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Password reset successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid reset code or expired")
            }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        resetPassword.execute(new ResetPasswordCommand(
                resetPasswordRequest.getEmail(),
                resetPasswordRequest.getCode(),
                resetPasswordRequest.getNewPassword()
        ));
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Request email verification",
            description = "Sends an email with a verification token to the user.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Verification email sent")
            }
    )
    @PostMapping("/request-email-verification")
    public ResponseEntity<Void> requestEmailVerification(
            @Parameter(description = "User email to verify") @RequestParam String email) {
        requestEmailVerification.execute(email);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Verify email address",
            description = "Verifies the user's email using the provided token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email verified successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
            }
    )
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Parameter(description = "Email verification token") @RequestParam String token) {
        verifyEmail.execute(token);
        return ResponseEntity.ok().build();
    }
}
