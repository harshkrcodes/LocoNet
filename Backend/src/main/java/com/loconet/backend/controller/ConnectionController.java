// File: Backend/src/main/java/com/loconet/backend/controller/ConnectionController.java
// UPDATED for Phase 6 identity-trust pass — every endpoint now derives the
// caller's identity from @AuthenticationPrincipal (populated by
// JwtAuthFilter) instead of a client-supplied body field or query param.
package com.loconet.backend.controller;

import com.loconet.backend.dto.ConnectionRequestDTO;
import com.loconet.backend.dto.ConnectionResponseDTO;
import com.loconet.backend.entity.MatchStatus;
import com.loconet.backend.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    /**
     * senderId comes from the JWT, not the request body — ConnectionRequestDTO
     * only carries receiverId now. Previously a caller could put ANY
     * senderId in the body and send a request "as" someone else; that's
     * what this closes.
     */
    @PostMapping("/request")
    public ResponseEntity<ConnectionResponseDTO> sendRequest(
            @AuthenticationPrincipal UUID senderId,
            @Valid @RequestBody ConnectionRequestDTO request
    ) {
        ConnectionResponseDTO created = connectionService.sendRequest(senderId, request.getReceiverId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * currentUserId is passed through to ConnectionService so it can
     * verify the caller is actually the connection's receiver — see
     * ConnectionService.respondToRequest()'s UnauthorizedConnectionActionException
     * check. This is a second loophole closed in this pass, not something
     * you explicitly listed, but it's the same bug: trusting an identity
     * the request didn't actually prove.
     */
    @PutMapping("/respond")
    public ResponseEntity<ConnectionResponseDTO> respond(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam UUID connectionId,
            @RequestParam MatchStatus status
    ) {
        ConnectionResponseDTO updated = connectionService.respondToRequest(connectionId, status, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ConnectionResponseDTO>> getPending(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(connectionService.getPendingRequests(userId));
    }
}
