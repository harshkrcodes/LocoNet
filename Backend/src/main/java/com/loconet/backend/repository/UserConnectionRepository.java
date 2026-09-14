// File: Backend/src/main/java/com/loconet/backend/repository/UserConnectionRepository.java
package com.loconet.backend.repository;

import com.loconet.backend.entity.MatchStatus;
import com.loconet.backend.entity.UserConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserConnectionRepository extends JpaRepository<UserConnection, UUID> {

    /**
     * Checks for a connection in EITHER direction between two users. The
     * DB's UNIQUE(sender_id, receiver_id) constraint alone would only stop
     * a literal repeat of (A,B) — it would still allow B to separately
     * request A. This JPQL query catches both directions so app-level
     * duplicate prevention actually matches "these two users already have
     * a connection", not just "this exact row already exists".
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM UserConnection c " +
            "WHERE (c.sender.id = :userAId AND c.receiver.id = :userBId) " +
            "OR (c.sender.id = :userBId AND c.receiver.id = :userAId)")
    boolean existsConnectionBetween(@Param("userAId") UUID userAId, @Param("userBId") UUID userBId);

    /**
     * Pending requests received by a user (i.e. this user is the receiver
     * and hasn't responded yet). "receiverId" resolves via Spring Data's
     * nested-property traversal to the receiver association's id field.
     */
    List<UserConnection> findByReceiverIdAndStatus(UUID receiverId, MatchStatus status);
}
