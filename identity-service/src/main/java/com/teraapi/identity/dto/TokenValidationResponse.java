package com.teraapi.identity.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Response contract shared with other services when they introspect a token.
 */
@Value
@Builder
public class TokenValidationResponse {

    boolean active;
    String username;
    String role;
    String deviceId;
    long issuedAt;
    long expiresAt;

    public static TokenValidationResponse inactive() {
        return TokenValidationResponse.builder().active(false).build();
    }
}
