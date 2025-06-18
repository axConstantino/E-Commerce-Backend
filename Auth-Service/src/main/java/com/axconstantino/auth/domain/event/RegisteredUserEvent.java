package com.axconstantino.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

public record RegisteredUserEvent(
        UUID userId,
        String name,
        String email,
        Instant timestamp
) {
    public RegisteredUserEvent (UUID userId, String name, String email) {
        this(userId, name, email, Instant.now());
    }
}
