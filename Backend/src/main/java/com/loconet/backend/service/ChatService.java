// File: Backend/src/main/java/com/loconet/backend/service/ChatService.java
package com.loconet.backend.service;

import com.loconet.backend.dto.ChatMessageDTO;
import com.loconet.backend.entity.ChatMessage;
import com.loconet.backend.entity.MessageStatus;
import com.loconet.backend.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * id/timestamp/status on the incoming DTO are ignored — the server is
     * the source of truth for all three (a client-supplied timestamp or
     * status would be trivially spoofable).
     */
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
}
