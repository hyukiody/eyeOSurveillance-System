package com.teraapi.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String role;

    public static AuthenticationResponse of(String accessToken, Long expiresIn, String username, String role) {
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .username(username)
                .role(role)
                .build();
    }
}
