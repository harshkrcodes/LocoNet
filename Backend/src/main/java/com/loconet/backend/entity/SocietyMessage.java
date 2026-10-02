// File: Backend/src/main/java/com/loconet/backend/entity/SocietyMessage.java
package com.loconet.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * societyId/senderId are plain UUID columns, same rationale as
 * ChatMessage's senderId/receiverId: avoids an entity fetch per message on
 * a high-volume table. No status field — you didn't ask for one, and
 * per-member read-receipts for a group chat are a different (bigger)
 * feature than 1-on-1 DELIVERED/READ.
 */
@Entity
@Table(name = "society_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocietyMessage {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "society_id", nullable = false)
    private UUID societyId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
