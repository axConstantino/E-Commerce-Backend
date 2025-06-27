package com.axconstantino.auth.application.usecase;

import jakarta.servlet.http.HttpServletRequest;

public interface Logout {
    void execute(HttpServletRequest request);
}
