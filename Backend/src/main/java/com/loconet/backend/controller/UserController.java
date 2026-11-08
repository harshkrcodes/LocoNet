package com.loconet.backend.controller;

import com.loconet.backend.dto.ProfileUpdateRequest;
import com.loconet.backend.dto.UserDTO;
import com.loconet.backend.dto.UserRegistrationRequest;
import com.loconet.backend.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserRegistrationRequest request) {
        UserDTO created = userService.registerUser(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        // 🔥 FIX: Cast to String and parse UUID, because Principal holds the ID, not the full User entity!
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {

        // 🔥 FIX: Cast to String and parse UUID, because Principal holds the ID, not the full User entity!
        UUID userId = UUID.fromString(authentication.getPrincipal().toString());
        UserDTO updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
}