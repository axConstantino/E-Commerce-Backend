package com.axconstantino.auth.domain.model;

import java.time.Instant;

public class Token {

    private final String token;
    private final TokenType type;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private final String ipAddress;
    private final String userAgent;
    private boolean active;
    private User user;

    public Token(String token, TokenType type, Instant issuedAt, Instant expiresAt,
                 String ipAddress, String userAgent, boolean active, User user) {
        this.token = token;
        this.type = type;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.active = active;
    }

    public String getToken() { return token; }
    public TokenType getTokenType() { return type; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public boolean isActive() { return active; }
    public User getUser() { return user; }


    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return active && !isExpired();
    }

    public void revoke() {
        this.active = false;
    }
}
