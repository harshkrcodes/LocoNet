// File: Backend/src/main/java/com/loconet/backend/dto/SocietyMessageDTO.java
package com.loconet.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inbound STOMP payload (id/timestamp ignored on the way in — ChatService
 * sets them) and outbound broadcast shape to /topic/society/{societyId}.
 *
 * UPDATED for Phase 6 identity-trust pass: senderId is no longer trusted
 * from an inbound payload — ChatController.handleSocietyChatMessage()
 * overwrites whatever's here with the authenticated STOMP session's user
 * id before saving.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocietyMessageDTO {

    private UUID id;

    @NotNull
    private UUID societyId;

    private UUID senderId;

    @NotBlank
    private String content;

    private LocalDateTime timestamp;
}
