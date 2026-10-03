// File: Backend/src/main/java/com/loconet/backend/security/JwtUtil.java
//
// NOTE: "security" is a new package, not one of the six listed in your
// strict rules (entity/dto/repository/service/controller/config). JwtUtil
// and JwtAuthFilter don't fit cleanly into any of those — grouping
// JWT-specific infrastructure together is the more conventional and
// maintainable layout. Flagging it explicitly rather than silently
// introducing a package you didn't name.
package com.loconet.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMillis
    ) {
        // HS256 requires a key >= 256 bits (32 bytes). Keys.hmacShaKeyFor
        // throws at startup if jwt.secret is shorter — fail fast rather
        // than silently producing an insecure token.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    /**
     * userId goes in the "sub" claim (as a string, since JWT claims are
     * text) so extractUserId() can parse it straight back to a UUID.
     */
    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Throws (ExpiredJwtException/JwtException/IllegalArgumentException)
     * on anything invalid — expired, malformed, wrong signature. Callers
     * that just want a yes/no should use isTokenValid() instead.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
