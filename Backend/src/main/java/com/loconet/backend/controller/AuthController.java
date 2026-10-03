// File: Backend/src/main/java/com/loconet/backend/controller/AuthController.java
package com.loconet.backend.controller;

import com.loconet.backend.dto.LoginRequestDTO;
import com.loconet.backend.dto.LoginResponseDTO;
import com.loconet.backend.exception.InvalidCredentialsException;
import com.loconet.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Placeholder credential for Phase 6 testing only — NOT real auth.
    // Replace this entire method body with UserRepository.findByEmail(...)
    // + PasswordEncoder.matches(...) once DB-backed login is wired up;
    // JwtUtil/JwtAuthFilter/StompAuthChannelInterceptor don't need to
    // change at all when that happens — only this check does.
    private static final String DUMMY_PASSWORD = "password123";

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        if (!DUMMY_PASSWORD.equals(request.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // No real user table lookup yet, so there's no stable UUID to use.
        // Deriving one deterministically from the email (rather than
        // UUID.randomUUID()) means the same test email always gets the
        // same userId across logins — useful since every other endpoint
        // in this backend is UUID-keyed.
        UUID fakeUserId = UUID.nameUUIDFromBytes(
                request.getEmail().toLowerCase().getBytes(StandardCharsets.UTF_8));

        String token = jwtUtil.generateToken(fakeUserId, request.getEmail());

        return ResponseEntity.ok(LoginResponseDTO.builder()
                .token(token)
                .userId(fakeUserId)
                .email(request.getEmail())
                .build());
    }
}
