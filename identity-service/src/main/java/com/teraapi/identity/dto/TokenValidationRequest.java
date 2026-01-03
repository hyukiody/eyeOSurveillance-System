package com.teraapi.identity.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Contract describing how downstream services ask identity-service to verify a JWT.
 */
public record TokenValidationRequest(@NotBlank(message = "token must not be blank") String token) {
}
