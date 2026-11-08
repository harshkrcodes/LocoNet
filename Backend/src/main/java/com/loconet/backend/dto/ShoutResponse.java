package com.loconet.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for sending shout data to the client, masking internal entity relationships.
 * Includes authorId to facilitate direct messaging from the local feed.
 */
@Data
@Builder
public class ShoutResponse {
    private UUID id;
    private UUID authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}