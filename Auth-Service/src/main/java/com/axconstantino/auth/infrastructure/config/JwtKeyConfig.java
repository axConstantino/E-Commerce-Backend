package com.axconstantino.auth.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtKeyConfig {
    @Value("${jwt.private-key}")
    private String privateKeyPem;

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    @Bean
    public PrivateKey jwtPrivateKey() {
        return parseRSAPrivateKey(privateKeyPem);
    }

    @Bean
    public PublicKey jwtPublicKey() {
        return parseRSAPublicKey(publicKeyPem);
    }

    private PrivateKey parseRSAPrivateKey(String pem) {
        try {
            String cleanPem = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                                    .replace("-----END PRIVATE-----", "")
                                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA private key", e);
        }
    }

    private PublicKey parseRSAPublicKey(String pem) {
        try {
            String cleanPem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                                    .replace("-----END PUBLIC KEY-----", "")
                                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid RSA public key", e);
        }
    }
}
