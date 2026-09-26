// File: Backend/src/main/java/com/loconet/backend/repository/ChatMessageRepository.java
package com.loconet.backend.repository;

import com.loconet.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT m FROM ChatMessage m " +
            "WHERE (m.senderId = :userAId AND m.receiverId = :userBId) " +
            "OR (m.senderId = :userBId AND m.receiverId = :userAId) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("userAId") UUID userAId, @Param("userBId") UUID userBId);
}
