package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.command.RegisterCommand;
import com.axconstantino.auth.application.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface RegisterUser {

    TokenResponse execute(RegisterCommand command, HttpServletRequest request);
}
