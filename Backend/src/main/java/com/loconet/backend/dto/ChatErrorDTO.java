// File: Backend/src/main/java/com/loconet/backend/dto/ChatErrorDTO.java
package com.loconet.backend.dto;

/**
 * Payload delivered to the sender's own /user/queue/errors when a STOMP SEND
 * is rejected. `code` is stable and machine-readable (clients switch on it);
 * `message` is human-readable and safe to show.
 */
public record ChatErrorDTO(String code, String message) {
}
