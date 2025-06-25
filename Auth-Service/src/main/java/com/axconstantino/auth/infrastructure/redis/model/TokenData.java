package com.axconstantino.auth.infrastructure.redis.model;

import java.time.Instant;

public record TokenData(
        String userId,
        String tokenValue,
        boolean active,
        Instant expiresAt,
        String ipAddress,
        String userAgent
) {}