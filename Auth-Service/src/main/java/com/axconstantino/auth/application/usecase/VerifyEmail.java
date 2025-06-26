package com.axconstantino.auth.application.usecase;

public interface VerifyEmail {
    void execute(String token);
}
