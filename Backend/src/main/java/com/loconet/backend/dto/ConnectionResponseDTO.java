// File: Backend/src/main/java/com/loconet/backend/dto/ConnectionResponseDTO.java
package com.loconet.backend.dto;

import com.loconet.backend.entity.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Returned by all three endpoints: the created request (POST /request),
 * the updated request (PUT /respond), and each row in the pending list
 * (GET /pending).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionResponseDTO {

    private UUID id;
    private UserSummaryDTO sender;
    private UserSummaryDTO receiver;
    private MatchStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
