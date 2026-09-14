// File: Backend/src/main/java/com/loconet/backend/dto/ConnectionRequestDTO.java
package com.loconet.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload for POST /api/connections/request. senderId is a request field
 * rather than pulled from a security context because no auth layer exists
 * yet in this codebase (same situation as LocationController's userId
 * param) — swap for @AuthenticationPrincipal once auth is added.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDTO {

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID receiverId;
}
