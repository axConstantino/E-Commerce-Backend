package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.AuthenticateCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface RegisterUser {

    TokenResponse execute(AuthenticateCommand command, HttpServletRequest request);
}
