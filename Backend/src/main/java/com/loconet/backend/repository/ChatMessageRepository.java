package com.loconet.backend.repository;

import com.loconet.backend.entity.ChatMessage;
import com.loconet.backend.dto.InboxProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // Teri purani query yahan rahegi...
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE (m.senderId = :userAId AND m.receiverId = :userBId) " +
            "OR (m.senderId = :userBId AND m.receiverId = :userAId) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("userAId") UUID userAId, @Param("userBId") UUID userBId);

    // YEH NAYA CODE ADD KARNA HAI: Inbox/Chat List nikalne ke liye
    @Query(value = """
        SELECT 
            u.id AS userId,
            u.full_name AS fullName,
            m.content AS lastMessage,
            m.timestamp AS timestamp,
            0 AS unreadCount
        FROM users u
        JOIN (
            SELECT DISTINCT ON (partner_id) 
                partner_id, content, timestamp
            FROM (
                SELECT receiver_id AS partner_id, content, timestamp FROM chat_messages WHERE sender_id = :userId
                UNION ALL
                SELECT sender_id AS partner_id, content, timestamp FROM chat_messages WHERE receiver_id = :userId
            ) combined
            ORDER BY partner_id, timestamp DESC
        ) m ON u.id = m.partner_id
        ORDER BY m.timestamp DESC
    """, nativeQuery = true)
    List<InboxProjection> getUserInbox(@Param("userId") UUID userId);
}