package com.teraapi.stream;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

/**
 * JWT Validation Utility for StreamProcessingService
 * Validates JWT tokens signed by IdentityService without database dependency
 */
public class JwtValidationUtil {

    private final String jwtSecret;

    public JwtValidationUtil(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("JWT token is unsupported: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.err.println("JWT signature validation failed: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (JwtException e) {
            System.err.println("Failed to extract username from token: " + e.getMessage());
            return null;
        }
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("role", String.class);
        } catch (JwtException e) {
            System.err.println("Failed to extract role from token: " + e.getMessage());
            return null;
        }
    }

    public String getDeviceIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("deviceId", String.class);
        } catch (JwtException e) {
            System.err.println("Failed to extract deviceId from token: " + e.getMessage());
            return null;
        }
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getPayload();
    }

    public String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
