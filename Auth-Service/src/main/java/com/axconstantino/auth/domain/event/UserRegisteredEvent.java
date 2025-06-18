package com.axconstantino.auth.domain.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String name,
        String email,
        Instant timestamp
) {
    public UserRegisteredEvent(UUID userId, String name, String email) {
        this(userId, name, email, Instant.now());
    }
}
