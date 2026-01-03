package com.teraapi.identity.service;

import com.teraapi.identity.dto.AuthenticationRequest;
import com.teraapi.identity.dto.AuthenticationResponse;
import com.teraapi.identity.dto.TokenValidationResponse;
import com.teraapi.identity.entity.Role;
import com.teraapi.identity.entity.User;
import com.teraapi.identity.repository.RoleRepository;
import com.teraapi.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws AuthenticationException {
        log.info("Authenticating user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().getName());
        log.info("Token generated successfully for user: {}", user.getUsername());

        return AuthenticationResponse.of(
                token,
                jwtTokenProvider.getExpirationTimeInSeconds(),
                user.getUsername(),
                user.getRole().getName()
        );
    }

    @Transactional
    public AuthenticationResponse register(User newUser) {
        log.info("Registering new user: {}", newUser.getUsername());

        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));
        newUser.setRole(userRole);

        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully: {}", savedUser.getUsername());

        String token = jwtTokenProvider.generateToken(savedUser.getUsername(), savedUser.getRole().getName());

        return AuthenticationResponse.of(
                token,
                jwtTokenProvider.getExpirationTimeInSeconds(),
                savedUser.getUsername(),
                savedUser.getRole().getName()
        );
    }

        @Transactional(readOnly = true)
        public TokenValidationResponse validateToken(String token) {
                if (!jwtTokenProvider.isTokenValid(token)) {
                        log.warn("Token validation failed because token is invalid");
                        return TokenValidationResponse.inactive();
                }

                String username = jwtTokenProvider.getUsernameFromToken(token);
                User user = userRepository.findByUsername(username).orElse(null);
                if (user == null || Boolean.FALSE.equals(user.getIsActive())) {
                        log.warn("Token validation failed because user {} is inactive or missing", username);
                        return TokenValidationResponse.inactive();
                }

                return TokenValidationResponse.builder()
                                .active(true)
                                .username(username)
                                .role(user.getRole().getName())
                                .deviceId(jwtTokenProvider.getDeviceIdFromToken(token))
                                .issuedAt(jwtTokenProvider.getIssuedAt(token).toInstant().toEpochMilli())
                                .expiresAt(jwtTokenProvider.getExpiration(token).toInstant().toEpochMilli())
                                .build();
        }
}
