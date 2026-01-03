package com.teraapi.controller;

import com.teraapi.entity.User;
import com.teraapi.entity.Session;
import com.teraapi.repository.UserRepository;
import com.teraapi.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        User user = new User(email, passwordEncoder.encode(password));
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        Session session = new Session(user.getId(), token, LocalDateTime.now().plusDays(7));
        sessionRepository.save(session);

        return ResponseEntity.status(201).body(Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "token", token
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String token = UUID.randomUUID().toString();
        Session session = new Session(user.getId(), token, LocalDateTime.now().plusDays(7));
        sessionRepository.save(session);

        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "token", token
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing token"));
        }

        String token = authHeader.substring(7);
        var sessionOpt = sessionRepository.findByToken(token);

        if (sessionOpt.isEmpty() || !sessionOpt.get().isValid()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        Session session = sessionOpt.get();
        var user = userRepository.findById(session.getUserId());

        return user.map(u -> ResponseEntity.ok(Map.of(
            "id", u.getId(),
            "email", u.getEmail()
        ))).orElse(ResponseEntity.status(401).body(Map.of("error", "User not found")));
    }
}
