package com.axconstantino.auth.infrastructure.redis.model;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("token")
public class RedisTokenEntity implements Serializable {
    private String token;
    private String userId;
    private boolean active;
    private Instant expiresAt;
    private String ipAddress;
    private String userAgent;
}
