package com.axconstantino.auth.application.command;

public record ResetPasswordCommand(String email, String code, String newPassword) {
}
