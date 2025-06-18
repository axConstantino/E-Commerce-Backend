package com.axconstantino.auth.application.command;

public record RegisterCommand(
        String name,
        String email,
        String password
) {}
