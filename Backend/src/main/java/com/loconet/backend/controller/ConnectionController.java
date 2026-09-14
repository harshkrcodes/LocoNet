// File: Backend/src/main/java/com/loconet/backend/controller/ConnectionController.java
package com.loconet.backend.controller;

import com.loconet.backend.dto.ConnectionRequestDTO;
import com.loconet.backend.dto.ConnectionResponseDTO;
import com.loconet.backend.entity.MatchStatus;
import com.loconet.backend.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/request")
    public ResponseEntity<ConnectionResponseDTO> sendRequest(@Valid @RequestBody ConnectionRequestDTO request) {
        ConnectionResponseDTO created = connectionService.sendRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * status is bound straight to the MatchStatus enum by Spring's default
     * enum converter (matches by name, e.g. ?status=ACCEPTED); the service
     * still rejects anything other than ACCEPTED/REJECTED (e.g. PENDING)
     * since that's a business rule, not a type-conversion concern.
     */
    @PutMapping("/respond")
    public ResponseEntity<ConnectionResponseDTO> respond(
            @RequestParam UUID connectionId,
            @RequestParam MatchStatus status
    ) {
        ConnectionResponseDTO updated = connectionService.respondToRequest(connectionId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * userId is a query param rather than pulled from a security context
     * because no authentication layer exists yet — same situation as
     * LocationController; swap for @AuthenticationPrincipal once auth lands.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionResponseDTO>> getPending(@RequestParam UUID userId) {
        return ResponseEntity.ok(connectionService.getPendingRequests(userId));
    }
}
