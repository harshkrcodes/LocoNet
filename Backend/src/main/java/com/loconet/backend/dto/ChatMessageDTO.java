// File: Backend/src/main/java/com/loconet/backend/dto/ChatMessageDTO.java
package com.loconet.backend.dto;

import com.loconet.backend.entity.MessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Doubles as the inbound STOMP payload (id/timestamp/status are ignored on
 * the way in — ChatService sets them) and the outbound shape (fully
 * populated) for both the WebSocket push and the REST history endpoint.
 *
 * UPDATED for Phase 6 identity-trust pass: senderId is no longer trusted
 * from an inbound payload — ChatController.handleChatMessage() overwrites
 * whatever's here with the authenticated STOMP session's user id before
 * saving. @NotNull dropped accordingly (it's still populated on the way
 * out, just not required on the way in).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    private UUID id;

    private UUID senderId;

    @NotNull
    private UUID receiverId;

    @NotBlank
    private String content;

    private LocalDateTime timestamp;

    private MessageStatus status;
}
