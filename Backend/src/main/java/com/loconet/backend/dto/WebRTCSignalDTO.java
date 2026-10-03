// File: Backend/src/main/java/com/loconet/backend/dto/WebRTCSignalDTO.java
package com.loconet.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Both the inbound STOMP payload and the outbound relayed shape for
 * /app/signal.send -> /user/{receiverId}/queue/signals.
 *
 * sdp and candidate are both nullable and mutually exclusive in practice:
 * OFFER/ANSWER populate sdp, ICE_CANDIDATE populates candidate. Not
 * enforced with a cross-field constraint here — SignalingController
 * relays whatever's present without inspecting payload semantics, same
 * "dumb relay" principle as the rest of this feature.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebRTCSignalDTO {

    @NotNull
    private SignalType type;

    // Ignored on input — SignalingController overwrites it with the
    // authenticated STOMP session's user id before relaying, same
    // identity-trust rule as ChatMessageDTO/SocietyMessageDTO's senderId.
    private UUID senderId;

    @NotNull
    private UUID receiverId;

    private String sdp;

    private String candidate;
}
