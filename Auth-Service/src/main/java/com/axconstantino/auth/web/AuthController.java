package com.axconstantino.auth.web;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.command.RegisterCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.LoginUser;
import com.axconstantino.auth.application.usecase.RefreshToken;
import com.axconstantino.auth.application.usecase.RegisterUser;
import com.axconstantino.auth.web.dto.AuthenticateRequest;
import com.axconstantino.auth.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUser loginUser;
    private final RegisterUser registerUser;
    private final RefreshToken refreshToken;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(RegisterRequest request, HttpServletRequest httpRequest) {
        RegisterCommand command = new RegisterCommand(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        final TokenResponse response = registerUser.execute(command, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> authenticate(AuthenticateRequest request, HttpServletRequest httpRequest) {
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
}
