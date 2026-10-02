// File: Backend/src/main/java/com/loconet/backend/repository/SocietyMessageRepository.java
package com.loconet.backend.repository;

import com.loconet.backend.entity.SocietyMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SocietyMessageRepository extends JpaRepository<SocietyMessage, UUID> {

    // Nothing calls this yet (you didn't ask for a GET history endpoint
    // this round) — it's here so the data's queryable as soon as a
    // /api/society-chat/history endpoint is wanted, same shape as
    // ChatMessageRepository.findChatHistory from Phase 5 Part 1.
    List<SocietyMessage> findBySocietyIdOrderByTimestampAsc(UUID societyId);
}
