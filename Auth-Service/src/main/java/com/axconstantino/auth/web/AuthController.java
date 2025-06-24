package com.axconstantino.auth.web;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.command.RegisterCommand;
import com.axconstantino.auth.application.command.ResetPasswordCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.*;
import com.axconstantino.auth.web.dto.AuthenticateRequest;
import com.axconstantino.auth.web.dto.ForgotPasswordRequest;
import com.axconstantino.auth.web.dto.RegisterRequest;
import com.axconstantino.auth.web.dto.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUser loginUser;
    private final RegisterUser registerUser;
    private final RefreshToken refreshToken;
    private final ResetPassword resetPassword;
    private final ForgotPassword forgotPassword;

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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(@Valid @RequestBody AuthenticateRequest request, HttpServletRequest httpRequest) {
        AuthenticateCommand command = new AuthenticateCommand(
                request.getEmail(),
                request.getPassword()
        );

        final TokenResponse response = loginUser.execute(command, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(
            HttpServletRequest httpRequest
    ) {
        TokenResponse response = refreshToken.execute(httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        forgotPassword.execute(forgotPasswordRequest.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        resetPassword.execute(new ResetPasswordCommand(
                resetPasswordRequest.getEmail(),
                resetPasswordRequest.getCode(),
                resetPasswordRequest.getNewPassword()
        ));

        return ResponseEntity.noContent().build();
    }
}
