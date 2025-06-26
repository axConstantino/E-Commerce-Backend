package com.axconstantino.auth.infrastructure.jwt;

import com.axconstantino.auth.domain.model.Role;
import com.axconstantino.auth.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Component responsible for generating, parsing and validating JWT tokens using RSA (RS256).
 * <p>
 * This provider supports access tokens, refresh tokens, and email verification tokens.
 * It uses asymmetric encryption (public/private key pair) for secure signing and verification.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.access-token.expiration-time}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpiration;

    /**
     * Extracts the subject (typically the user's email) from the JWT.
     *
     * @param token the JWT
     * @return the subject (username/email)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID embedded in the token.
     *
     * @param token the JWT
     * @return user ID as String
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Extracts the issued date from the JWT.
     *
     * @param token the JWT
     * @return issue date
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Generates a JWT access token for the authenticated user.
     *
     * @param user the user entity
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        String roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.joining(","));
        extraClaims.put("roles", roles);
        extraClaims.put("userId", user.getId().toString());
        extraClaims.put("username", user.getUserName());

        String token = buildToken(extraClaims, user.getEmail(), accessTokenExpiration);
        log.info("Access token generated for user: {}", user.getEmail());
        return token;
    }

    /**
     * Generates a JWT refresh token for the user.
     *
     * @param user the user entity
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId().toString());

        String token = buildToken(extraClaims, user.getEmail(), refreshTokenExpiration);
        log.info("Refresh token generated for user: {}", user.getEmail());
        return token;
    }

    /**
     * Generates a one-time JWT token for email verification.
     *
     * @param email      the user's email
     * @param userId     the user's ID
     * @param expiration expiration time in milliseconds
     * @return email verification token
     */
    public String generateEmailVerificationToken(String email, UUID userId, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("type", "email-verification");

        String token = buildToken(claims, email, expiration);
        log.info("Email verification token generated for email: {}", email);
        return token;
    }

    /**
     * Validates the token against the provided user.
     *
     * @param token the JWT
     * @param user  the user entity
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(user.getEmail()) && !isTokenExpired(token);
            if (!isValid) {
                log.warn("Token is invalid or expired for user: {}", user.getEmail());
            }
            return isValid;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if the token has expired.
     *
     * @param token the JWT
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim using the provided resolver function.
     *
     * @param token          the JWT
     * @param claimsResolver resolver for extracting a specific claim
     * @param <T>            the type of the claim
     * @return the extracted claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the JWT and extracts all claims.
     *
     * @param token the JWT
     * @return all claims
     * @throws io.jsonwebtoken.JwtException if parsing fails
     */
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to extract claims from token: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Builds a signed JWT token using RS256 algorithm.
     *
     * @param extraClaims custom claims to include in the token
     * @param subject     token subject (typically user's email)
     * @param expiration  expiration in milliseconds
     * @return signed JWT string
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
