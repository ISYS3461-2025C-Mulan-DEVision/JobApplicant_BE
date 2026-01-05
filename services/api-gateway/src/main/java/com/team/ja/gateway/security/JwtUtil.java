package com.team.ja.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Utility for validating tokens in API Gateway.
 * Handles nested JWS-then-JWE tokens.
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String signSecretKey;

    @Value("${jwe.secret}")
    private String jweSecretKey;

    public boolean validateToken(String jweString) {
        try {
            String jwsString = decryptJwe(jweString);
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .setAllowedClockSkewSeconds(300) // 5 min clock skew
                    .build()
                    .parseSignedClaims(jwsString);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public Claims extractAllClaims(String jweString) {
        String jwsString = decryptJwe(jweString);
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwsString)
                .getPayload();
    }

    public String extractUserId(String jweString) {
        return extractAllClaims(jweString).get("userId", String.class);
    }

    public String extractUsername(String jweString) {
        return extractAllClaims(jweString).getSubject();
    }

    public String extrachCountry(String jweString) {
        return extractAllClaims(jweString).get("country", String.class);
    }

    public String extractRole(String jweString) {
        return extractAllClaims(jweString).get("role", String.class);
    }

    public String extractJti(String jweString) {
        return extractAllClaims(jweString).getId();
    }

    public boolean isTokenExpired(String jweString) {
        try {
            return extractAllClaims(jweString).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private String decryptJwe(String jweString) {
        try {
            Jwe<byte[]> jwe = Jwts.parser()
                    .decryptWith(getEncryptionKey())
                    .build()
                    .parseEncryptedContent(jweString);
            return new String(jwe.getPayload(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("JWE decryption failed: {}", e.getMessage());
            throw new MalformedJwtException("Unable to decrypt JWE", e);
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(signSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getEncryptionKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jweSecretKey);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
