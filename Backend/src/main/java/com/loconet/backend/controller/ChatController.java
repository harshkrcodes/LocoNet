// File: Backend/src/main/java/com/loconet/backend/controller/ChatController.java
// UPDATED — society membership lockdown:
//  - getSocietyHistory now takes @AuthenticationPrincipal and passes it to
//    ChatService.getSocietyHistory() as the requester to check membership
//    against (was previously open to any authenticated user for any
//    societyId).
//  - Added a @MessageExceptionHandler for NotSocietyMemberException. Without
//    it, a rejected STOMP SEND to /app/society.chat would just throw,
//    Spring would log it, and the sender would get no feedback at all —
//    not true 403 semantics, which is what @ResponseStatus gives us for
//    the REST paths. This sends the rejection back to just that sender's
//    /user/queue/errors.
package com.loconet.backend.controller;

import com.loconet.backend.dto.ChatMessageDTO;
import com.loconet.backend.dto.SocietyMessageDTO;
import com.loconet.backend.exception.NotSocietyMemberException;
import com.loconet.backend.security.StompPrincipalResolver;
import com.loconet.backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
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

    // ---- 1-on-1 ----

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessageDTO incoming, Principal principal) {
        UUID senderId = StompPrincipalResolver.resolveUserId(principal);

        ChatMessageDTO verified = ChatMessageDTO.builder()
                .senderId(senderId)
                .receiverId(incoming.getReceiverId())
                .content(incoming.getContent())
                .build();

        ChatMessageDTO saved = chatService.saveMessage(verified);

        messagingTemplate.convertAndSendToUser(
                saved.getReceiverId().toString(),
                "/queue/messages",
                saved
        );
    }

    @GetMapping("/api/chat/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam UUID otherUserId
    ) {
        return ResponseEntity.ok(chatService.getChatHistory(currentUserId, otherUserId));
    }

    // ---- Society / group chat ----

    /**
     * senderId still comes from the Principal (Phase 6 identity-trust
     * pass), and ChatService now additionally verifies that sender is a
     * verified member of the target societyId before saving — see
     * handleSocietyChatException() below for what happens when they're not.
     */
    @MessageMapping("/society.chat")
    public void handleSocietyChatMessage(SocietyMessageDTO incoming, Principal principal) {
        UUID senderId = StompPrincipalResolver.resolveUserId(principal);

        SocietyMessageDTO verified = SocietyMessageDTO.builder()
                .societyId(incoming.getSocietyId())
                .senderId(senderId)
                .content(incoming.getContent())
                .build();

        SocietyMessageDTO saved = chatService.saveSocietyMessage(verified);

        messagingTemplate.convertAndSend(
                "/topic/society/" + saved.getSocietyId(),
                saved
        );
    }

    /**
     * Catches NotSocietyMemberException thrown anywhere during handling of
     * a @MessageMapping method in this controller (i.e. from
     * ChatService.saveSocietyMessage's membership check). @SendToUser
     * routes the return value to this session's own /user/queue/errors —
     * only the rejected sender sees it, nothing broadcasts to the society.
     */
    @MessageExceptionHandler(NotSocietyMemberException.class)
    @SendToUser("/queue/errors")
    public String handleSocietyChatException(NotSocietyMemberException ex) {
        return ex.getMessage();
    }

    /**
     * Requester must themselves be a verified member of societyId to read
     * its history — previously any authenticated user could pass any
     * societyId here.
     */
    @GetMapping("/api/society-chat/history")
    public ResponseEntity<List<SocietyMessageDTO>> getSocietyHistory(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam UUID societyId
    ) {
        return ResponseEntity.ok(chatService.getSocietyHistory(currentUserId, societyId));
    }
}
