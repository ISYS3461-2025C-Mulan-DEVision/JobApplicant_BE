package com.team.ja.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.KeyAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT Token Service for generating and validating tokens.
 * Creates nested JWS-then-JWE tokens.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String signSecretKey;

    @Value("${jwe.secret}")
    private String jweSecretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final KeyAlgorithm<SecretKey, SecretKey> KEY_ALG = Jwts.KEY.DIRECT;
    private static final AeadAlgorithm ENC_ALG = Jwts.ENC.A256GCM;

    public String extractUsername(String jweString) {
        return extractClaim(jweString, Claims::getSubject);
    }

    public UUID extractUserId(String jweString) {
        String userId = extractClaim(jweString, claims -> claims.get("userId", String.class));
        return userId != null ? UUID.fromString(userId) : null;
    }

    public String extractRole(String jweString) {
        return extractClaim(jweString, claims -> claims.get("role", String.class));
    }

    public String extractJti(String jweString) {
        return extractClaim(jweString, Claims::getId);
    }

    public Date extractExpiration(String jweString) {
        return extractClaim(jweString, Claims::getExpiration);
    }

    public <T> T extractClaim(String jweString, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jweString);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails, UUID userId, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId != null ? userId.toString() : null);
        extraClaims.put("role", role);
        extraClaims.put("type", "access");
        String jwsString = buildJws(extraClaims, userDetails, accessTokenExpiration);
        return encryptJws(jwsString);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("type", "refresh");
        String jwsString = buildJws(extraClaims, userDetails, refreshTokenExpiration);
        return encryptJws(jwsString);
    }

    public boolean isTokenValid(String jweString, UserDetails userDetails) {
        try {
            final String username = extractUsername(jweString);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(jweString);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String jweString) {
        return extractExpiration(jweString).before(new Date());
    }

    public boolean isRefreshToken(String jweString) {
        try {
            String type = extractClaim(jweString, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch(Exception e) {
            return false;
        }
    }

    private String buildJws(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .id(UUID.randomUUID().toString()) // JTI
                .signWith(getSigningKey())
                .compact();
    }

    private String encryptJws(String jwsString) {
        return Jwts.builder()
                .content(jwsString, "application/jwt") // Set content and content type
                .encryptWith(getEncryptionKey(), KEY_ALG, ENC_ALG)
                .compact();
    }

    private Claims extractAllClaims(String jweString) {
        Jwe<byte[]> jwe = Jwts.parser()
                .decryptWith(getEncryptionKey())
                .build()
                .parseEncryptedContent(jweString);

        String jwsString = new String(jwe.getPayload(), StandardCharsets.UTF_8);

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(jwsString)
                .getPayload();
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


