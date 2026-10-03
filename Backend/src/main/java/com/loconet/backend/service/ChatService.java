// File: Backend/src/main/java/com/loconet/backend/service/ChatService.java
// UPDATED — added UserSocietyRepository dependency and requireVerifiedMember(),
// called from both saveSocietyMessage() and getSocietyHistory() so a user
// can only post to or read a society's chat if they're a verified member
// of it. 1-on-1 saveMessage()/getChatHistory() are unchanged.
package com.loconet.backend.service;

import com.loconet.backend.dto.ChatMessageDTO;
import com.loconet.backend.dto.SocietyMessageDTO;
import com.loconet.backend.entity.ChatMessage;
import com.loconet.backend.entity.MessageStatus;
import com.loconet.backend.entity.SocietyMessage;
import com.loconet.backend.exception.NotSocietyMemberException;
import com.loconet.backend.repository.ChatMessageRepository;
import com.loconet.backend.repository.SocietyMessageRepository;
import com.loconet.backend.repository.UserSocietyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    // No status besides "PENDING" is ever written anywhere in this
    // codebase yet (Phase 3's onboarding flow sets it, nothing moves it
    // forward) — this check is correct as requested, but will reject
    // every user until a verification endpoint exists to set this value.
    private static final String VERIFIED_STATUS = "VERIFIED";

    private final ChatMessageRepository chatMessageRepository;
    private final SocietyMessageRepository societyMessageRepository;
    private final UserSocietyRepository userSocietyRepository;

    public ChatService(ChatMessageRepository chatMessageRepository,
                        SocietyMessageRepository societyMessageRepository,
                        UserSocietyRepository userSocietyRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.societyMessageRepository = societyMessageRepository;
        this.userSocietyRepository = userSocietyRepository;
    }

    // ---- 1-on-1 (unchanged) ----

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

    // ---- Society / group chat ----

    @Transactional
    public SocietyMessageDTO saveSocietyMessage(SocietyMessageDTO incoming) {
        requireVerifiedMember(incoming.getSenderId(), incoming.getSocietyId());

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
    public List<SocietyMessageDTO> getSocietyHistory(UUID requesterId, UUID societyId) {
        requireVerifiedMember(requesterId, societyId);

        return societyMessageRepository.findBySocietyIdOrderByTimestampAsc(societyId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Shared by both the read and write paths, per the requirement that
     * membership gates both. Throws rather than returning a boolean so
     * callers can't accidentally forget to check the result.
     */
    private void requireVerifiedMember(UUID userId, UUID societyId) {
        boolean isVerifiedMember = userSocietyRepository
                .existsByUser_IdAndSociety_IdAndVerificationStatus(userId, societyId, VERIFIED_STATUS);

        if (!isVerifiedMember) {
            throw new NotSocietyMemberException(
                    "User " + userId + " is not a verified member of society " + societyId);
        }
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
