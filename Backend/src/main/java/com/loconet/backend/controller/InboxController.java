package com.loconet.backend.controller;

import com.loconet.backend.dto.InboxProjection;
import com.loconet.backend.repository.ChatMessageRepository;
import com.loconet.backend.security.StompPrincipalResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
public class InboxController {

    private final ChatMessageRepository chatMessageRepository;

    public InboxController(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @GetMapping("/api/inbox")
    public ResponseEntity<List<InboxProjection>> getInbox(Principal principal) {
        // Token se current user ki ID nikali
        UUID currentUserId = StompPrincipalResolver.resolveUserId(principal);

        // Database se Inbox list fetch ki
        List<InboxProjection> inboxList = chatMessageRepository.getUserInbox(currentUserId);

        return ResponseEntity.ok(inboxList);
    }
}