package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginUser {
    TokenResponse execute(AuthenticateCommand command, HttpServletRequest httpRequest);
}
