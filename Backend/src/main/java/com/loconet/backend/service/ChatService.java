// File: Backend/src/main/java/com/loconet/backend/service/ChatService.java
// UPDATED for Phase 5 Part 2 — added SocietyMessageRepository dependency,
// saveSocietyMessage(), and its toDto mapper. The existing 1-on-1
// saveMessage()/getChatHistory() methods are unchanged.
package com.loconet.backend.service;

import com.loconet.backend.dto.ChatMessageDTO;
import com.loconet.backend.dto.SocietyMessageDTO;
import com.loconet.backend.entity.ChatMessage;
import com.loconet.backend.entity.MessageStatus;
import com.loconet.backend.entity.SocietyMessage;
import com.loconet.backend.repository.ChatMessageRepository;
import com.loconet.backend.repository.SocietyMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final SocietyMessageRepository societyMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                        SocietyMessageRepository societyMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.societyMessageRepository = societyMessageRepository;
    }

    // ---- 1-on-1 (Phase 5 Part 1, unchanged) ----

    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO incoming) {
        ChatMessage message = ChatMessage.builder()
                .senderId(incoming.getSenderId())
                .receiverId(incoming.getReceiverId())
                .content(incoming.getContent())
                .timestamp(LocalDateTime.now())
                .status(MessageStatus.SENT)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(UUID userAId, UUID userBId) {
        return chatMessageRepository.findChatHistory(userAId, userBId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ChatMessageDTO toDto(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .build();
    }

    // ---- Society / group chat (Phase 5 Part 2, new) ----

    /**
     * id/timestamp on the incoming DTO are ignored — server-set, same
     * reasoning as saveMessage(): a client-supplied timestamp is spoofable.
     * No membership check here (e.g. "is senderId actually in societyId");
     * that belongs in a dedicated membership-validation step if you want
     * one — currently anyone can post to any societyId they know.
     */
    @Transactional
    public SocietyMessageDTO saveSocietyMessage(SocietyMessageDTO incoming) {
        SocietyMessage message = SocietyMessage.builder()
                .societyId(incoming.getSocietyId())
                .senderId(incoming.getSenderId())
                .content(incoming.getContent())
                .timestamp(LocalDateTime.now())
                .build();

        SocietyMessage saved = societyMessageRepository.save(message);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SocietyMessageDTO> getSocietyHistory(UUID societyId) {
        return societyMessageRepository.findBySocietyIdOrderByTimestampAsc(societyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SocietyMessageDTO toDto(SocietyMessage message) {
        return SocietyMessageDTO.builder()
                .id(message.getId())
                .societyId(message.getSocietyId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}
