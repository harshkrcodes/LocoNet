// File: Backend/src/main/java/com/loconet/backend/controller/ChatController.java
// UPDATED for Phase 5 Part 2 — added the "/society.chat" @MessageMapping.
// The existing "/chat" mapping and REST history endpoint are unchanged.
package com.loconet.backend.controller;

import com.loconet.backend.dto.ChatMessageDTO;
import com.loconet.backend.dto.SocietyMessageDTO;
import com.loconet.backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    // ---- 1-on-1 (Phase 5 Part 1, unchanged) ----

    /**
     * Client SENDs to /app/chat (the "/app" prefix comes from
     * WebSocketConfig's setApplicationDestinationPrefixes).
     */
    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageDTO incoming) {
        ChatMessageDTO saved = chatService.saveMessage(incoming);

        messagingTemplate.convertAndSendToUser(
                saved.getReceiverId().toString(),
                "/queue/messages",
                saved
        );
    }

    @GetMapping("/api/chat/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @RequestParam UUID userAId,
            @RequestParam UUID userBId
    ) {
        return ResponseEntity.ok(chatService.getChatHistory(userAId, userBId));
    }

    // ---- Society / group chat (Phase 5 Part 2, new) ----

    /**
     * Client SENDs to /app/society.chat. Saves first, then broadcasts the
     * persisted (server-timestamped) version to every subscriber of
     * /topic/society/{societyId} — same save-then-push ordering as 1-on-1
     * chat, so nothing gets broadcast that failed to persist.
     *
     * Requires "/topic" to be registered in WebSocketConfig's
     * enableSimpleBroker(...) — see that file's update.
     */
    @MessageMapping("/society.chat")
    public void handleSocietyChatMessage(SocietyMessageDTO incoming) {
        SocietyMessageDTO saved = chatService.saveSocietyMessage(incoming);

        messagingTemplate.convertAndSend(
                "/topic/society/" + saved.getSocietyId(),
                saved
        );
    }

    @GetMapping("/api/society-chat/history")
    public ResponseEntity<List<SocietyMessageDTO>> getSocietyHistory(
            @RequestParam UUID societyId
    ) {
        return ResponseEntity.ok(chatService.getSocietyHistory(societyId));
    }
}
