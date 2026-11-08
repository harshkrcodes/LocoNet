package com.loconet.backend.controller;

import com.loconet.backend.dto.ShoutRequest;
import com.loconet.backend.dto.ShoutResponse;
import com.loconet.backend.service.ShoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/shouts")
@RequiredArgsConstructor
public class ShoutController {

    private final ShoutService shoutService;

    /**
     * Endpoint to broadcast a new shout to nearby users.
     */
    @PostMapping
    public ResponseEntity<ShoutResponse> createShout(@RequestBody ShoutRequest request, Principal principal) {
        return ResponseEntity.ok(shoutService.createShout(principal.getName(), request));
    }

    /**
     * Endpoint to fetch the local feed based on the user's current geographic coordinates.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<ShoutResponse>> getNearbyShouts(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(shoutService.getNearbyShouts(latitude, longitude));
    }
}