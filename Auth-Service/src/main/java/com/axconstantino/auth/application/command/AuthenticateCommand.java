package com.axconstantino.auth.application.command;

public record AuthenticateCommand(
        String email,
        String password) {
}
