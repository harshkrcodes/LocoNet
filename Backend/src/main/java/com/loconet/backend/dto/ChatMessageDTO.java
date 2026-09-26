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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    private UUID id;

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID receiverId;

    @NotBlank
    private String content;

    private LocalDateTime timestamp;

    private MessageStatus status;
}
