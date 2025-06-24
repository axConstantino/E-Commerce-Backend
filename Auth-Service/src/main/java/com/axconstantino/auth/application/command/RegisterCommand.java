package com.axconstantino.auth.application.command;

public record RegisterCommand(
        String userName,
        String email,
        String password
) {}
