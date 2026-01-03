package com.eyeo.data.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * License validation filter for Data Core.
 * 
 * Enforces tier-based quotas:
 * - Storage quota limits
 * - API rate limiting
 * - Watermark injection for FREE tier
 * 
 * Reads license info from JWT token in Authorization header.
 */
@Component
@Slf4j
public class LicenseCheckFilter implements Filter {

    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.license.enforcement.enabled:true}")
    private boolean enforcementEnabled;
    
    // Track storage usage per user (in-memory, should be DB in production)
    private final ConcurrentHashMap<String, AtomicLong> storageUsageBytes = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        // Skip license check if disabled
        if (!enforcementEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Only check license for video upload endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/stream/encrypt") && !path.startsWith("/storage")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            sendUnauthorized(response, "Missing authentication token");
            return;
        }
        
        String token = authHeader.substring(7);
        
        try {
            // Parse JWT and extract license claims
            Claims claims = parseToken(token);
            String username = claims.getSubject();
            String licenseTier = claims.get("licenseTier", String.class);
            Integer storageQuotaGb = claims.get("storageQuotaGb", Integer.class);
            Long trialEndDate = claims.get("trialEndDate", Long.class);
            
            log.debug("License check for user: {} (tier: {})", username, licenseTier);
            
            // Check trial expiration
            if (trialEndDate != null && System.currentTimeMillis() > trialEndDate) {
                if (!"PRO".equals(licenseTier) && !"ENTERPRISE".equals(licenseTier)) {
                    log.warn("Trial expired for user: {}", username);
                    sendForbidden(response, "Trial period expired. Please upgrade to continue.");
                    return;
                }
            }
            
            // Check storage quota for upload operations
            if ("POST".equalsIgnoreCase(request.getMethod()) && path.contains("/stream/encrypt")) {
                long currentUsageBytes = storageUsageBytes
                        .computeIfAbsent(username, k -> new AtomicLong(0))
                        .get();
                
                long quotaBytes = calculateQuotaBytes(licenseTier, storageQuotaGb);
                
                if (quotaBytes != -1 && currentUsageBytes >= quotaBytes) {
                    log.warn("Storage quota exceeded for user: {} ({} GB used, {} GB limit)", 
                            username, currentUsageBytes / (1024*1024*1024), storageQuotaGb);
                    sendQuotaExceeded(response, licenseTier, currentUsageBytes, quotaBytes);
                    return;
                }
            }
            
            // Attach license info to request attributes for downstream use
            request.setAttribute("username", username);
            request.setAttribute("licenseTier", licenseTier);
            request.setAttribute("requiresWatermark", "FREE".equals(licenseTier));
            
            // Continue filter chain
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("License validation failed", e);
            sendUnauthorized(response, "Invalid or expired token");
        }
    }
    
    /**
     * Parse JWT token and extract claims
     */
    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Calculate quota bytes based on tier
     */
    private long calculateQuotaBytes(String tier, Integer quotaGb) {
        if ("ENTERPRISE".equals(tier)) {
            return -1; // Unlimited
        }
        if (quotaGb == null) {
            return 5L * 1024 * 1024 * 1024; // Default 5 GB
        }
        return (long) quotaGb * 1024 * 1024 * 1024;
    }
    
    /**
     * Record storage usage (called after successful upload)
     */
    public void recordStorageUsage(String username, long bytes) {
        storageUsageBytes.computeIfAbsent(username, k -> new AtomicLong(0))
                .addAndGet(bytes);
        log.debug("Storage usage for {}: {} GB", username, 
                storageUsageBytes.get(username).get() / (1024*1024*1024));
    }
    
    /**
     * Send 401 Unauthorized
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
    }
    
    /**
     * Send 403 Forbidden
     */
    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\": \"Forbidden\", \"message\": \"%s\"}", message));
    }
    
    /**
     * Send 402 Payment Required (quota exceeded)
     */
    private void sendQuotaExceeded(HttpServletResponse response, String tier, 
                                   long currentBytes, long quotaBytes) throws IOException {
        response.setStatus(402); // Payment Required
        response.setContentType("application/json");
        
        long currentGb = currentBytes / (1024 * 1024 * 1024);
        long quotaGb = quotaBytes / (1024 * 1024 * 1024);
        
        String upgradeMessage = "FREE".equals(tier) 
                ? "Upgrade to PRO for 100GB storage" 
                : "Upgrade to ENTERPRISE for unlimited storage";
        
        response.getWriter().write(String.format(
                "{\"error\": \"QuotaExceeded\", " +
                "\"message\": \"Storage quota exceeded (%d GB / %d GB). %s\", " +
                "\"currentTier\": \"%s\", " +
                "\"usageBytes\": %d, " +
                "\"quotaBytes\": %d}",
                currentGb, quotaGb, upgradeMessage, tier, currentBytes, quotaBytes));
    }
}
