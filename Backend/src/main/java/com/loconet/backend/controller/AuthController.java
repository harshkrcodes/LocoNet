package com.loconet.backend.controller;

import com.loconet.backend.dto.LoginRequestDTO;
import com.loconet.backend.dto.LoginResponseDTO;
import com.loconet.backend.entity.User;
import com.loconet.backend.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {

        // 1. Email lowercase taaki 'Gemini' aur 'gemini' conflict na karein
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        // 2. AuthenticationManager ab DB se password verify karega (CustomUserDetailsService ke through)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. UserDetails (jo actually hamari User entity hai) se data nikalo
        User user = (User) authentication.getPrincipal();

        // 4. Real token generate karo
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .build());
    }
}