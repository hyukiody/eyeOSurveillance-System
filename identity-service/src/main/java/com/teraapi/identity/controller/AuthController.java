/*
 * TeraAPI - Identity Service
 * Copyright (c) 2026 YiStudIo Software Inc. All rights reserved.
 * Licensed under proprietary license.
 */
package com.teraapi.identity.controller;

import com.teraapi.identity.dto.AuthenticationRequest;
import com.teraapi.identity.dto.AuthenticationResponse;
import com.teraapi.identity.dto.TokenValidationRequest;
import com.teraapi.identity.dto.TokenValidationResponse;
import com.teraapi.identity.entity.User;
import com.teraapi.identity.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        try {
            log.info("Login request for user: {}", request.getUsername());
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest request) {
        try {
            log.info("Register request for user: {}", request.getUsername());
            User newUser = User.builder()
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .email(request.getUsername() + "@example.com")
                    .isActive(true)
                    .isLocked(false)
                    .build();
            
            AuthenticationResponse response = authenticationService.register(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Identity Service is running");
    }

    @PostMapping("/introspect")
    public ResponseEntity<TokenValidationResponse> introspect(
            @Valid @RequestBody TokenValidationRequest request) {
        log.info("Token introspection request received from downstream service");
        TokenValidationResponse response = authenticationService.validateToken(request.token());
        HttpStatus status = response.isActive() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }
}
