package com.axconstantino.auth.web;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import com.axconstantino.auth.application.usecase.RegisterUser;
import com.axconstantino.auth.web.dto.AuthenticateRequest;
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

    private final RegisterUser registerUser;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(AuthenticateRequest request, HttpServletRequest httpRequest) {
        AuthenticateCommand command = new AuthenticateCommand(
                request.getEmail(),
                request.getPassword()
        );
        TokenResponse response = registerUser.execute(command, httpRequest);
        return ResponseEntity.ok(response);
    }
}
