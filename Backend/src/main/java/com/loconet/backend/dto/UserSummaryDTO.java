// File: Backend/src/main/java/com/loconet/backend/dto/UserSummaryDTO.java
package com.loconet.backend.dto;

import com.loconet.backend.entity.UserIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Minimal, safe-to-expose user shape for embedding sender/receiver details
 * inside ConnectionResponseDTO. Deliberately excludes email, phoneNumber,
 * passwordHash, and location — a connection request doesn't need to leak
 * that to the other party.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {

    private UUID id;
    private String fullName;
    private String userTier;
    private UserIntent intent;
}
