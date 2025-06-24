package com.axconstantino.auth.domain.event;

public record PasswordResetEvent(String email, String code) {
}
