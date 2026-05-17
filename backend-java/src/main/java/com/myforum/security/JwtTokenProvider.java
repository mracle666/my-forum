package com.myforum.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-ttl}") String accessTtl,
                            @Value("${jwt.refresh-ttl}") String refreshTtl) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = parseDuration(accessTtl);
        this.refreshTtl = parseDuration(refreshTtl);
    }

    public String generateAccessToken(Long userId, String username, Integer role) {
        return generateToken(userId, username, role, accessTtl);
    }

    public String generateRefreshToken(Long userId, String username, Integer role) {
        return generateToken(userId, username, role, refreshTtl);
    }

    private String generateToken(Long userId, String username, Integer role, Duration ttl) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl.toMillis());

        return Jwts.builder()
                .claim("user_id", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public JwtClaims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setUserId(claims.get("user_id", Long.class));
            jwtClaims.setUsername(claims.get("username", String.class));
            Object roleObj = claims.get("role");
            jwtClaims.setRole(roleObj != null ? ((Number) roleObj).intValue() : 0);
            return jwtClaims;
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    private Duration parseDuration(String s) {
        s = s.trim();
        if (s.endsWith("h")) return Duration.ofHours(Long.parseLong(s.replace("h", "")));
        if (s.endsWith("m")) return Duration.ofMinutes(Long.parseLong(s.replace("m", "")));
        if (s.endsWith("d")) return Duration.ofDays(Long.parseLong(s.replace("d", "")));
        return Duration.ofHours(2);
    }
}
