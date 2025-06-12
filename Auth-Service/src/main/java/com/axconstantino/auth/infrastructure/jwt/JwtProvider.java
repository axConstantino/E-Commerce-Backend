package com.axconstantino.auth.infrastructure.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private final String secretKey;

    @Value("${jwt.expiration-time}")
    private final long expirationTime;

    @Value("${jwt.issuer}")
    private final String issuer;

}
