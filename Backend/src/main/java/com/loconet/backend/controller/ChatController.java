// File: Backend/src/main/java/com/loconet/backend/controller/ChatController.java
package com.loconet.backend.controller;

import com.loconet.backend.dto.ChatMessageDTO;
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

    /**
     * Client SENDs to /app/chat (the "/app" prefix comes from
     * WebSocketConfig's setApplicationDestinationPrefixes — don't include
     * it in the @MessageMapping value itself).
     *
     * Saves first, then pushes the persisted (server-timestamped) version
     * so sender and receiver both see the same canonical message, and the
     * receiver never gets a message that failed to persist.
     */
    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageDTO incoming) {
        ChatMessageDTO saved = chatService.saveMessage(incoming);

        // Resolves to /user/{receiverId}/queue/messages for the specific
        // receiver's session, via the Principal set in UserHandshakeHandler.
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
}
