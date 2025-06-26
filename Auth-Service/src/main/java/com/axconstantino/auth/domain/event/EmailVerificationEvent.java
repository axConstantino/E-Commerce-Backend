package com.axconstantino.auth.domain.event;

public record EmailVerificationEvent(String email, String verificationLink) {
}
