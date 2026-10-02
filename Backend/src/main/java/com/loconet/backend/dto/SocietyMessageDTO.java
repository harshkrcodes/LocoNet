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
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocietyMessageDTO {

    private UUID id;

    @NotNull
    private UUID societyId;

    @NotNull
    private UUID senderId;

    @NotBlank
    private String content;

    private LocalDateTime timestamp;
}
