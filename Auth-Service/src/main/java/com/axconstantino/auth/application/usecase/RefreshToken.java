package com.axconstantino.auth.application.usecase;

import com.axconstantino.auth.application.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface RefreshToken {
    TokenResponse execute(HttpServletRequest httpRequest);
}
