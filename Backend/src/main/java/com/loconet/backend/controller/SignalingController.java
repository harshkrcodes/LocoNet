// File: Backend/src/main/java/com/loconet/backend/controller/SignalingController.java
package com.loconet.backend.controller;

import com.loconet.backend.dto.WebRTCSignalDTO;
import com.loconet.backend.security.StompPrincipalResolver;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Pure WebRTC signaling relay — no entity, repository, or service layer.
 * Offers/answers/ICE candidates are ephemeral by nature (a stale SDP is
 * useless once a call connects or times out), so unlike ChatController
 * there's nothing here to persist.
 *
 * Plain @Controller rather than @RestController: this class has no REST
 * endpoints, only a STOMP @MessageMapping, so there's no @ResponseBody
 * semantics to opt into. (ChatController is @RestController only because
 * it also serves REST history endpoints alongside its @MessageMapping
 * methods.)
 */
@Controller
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    public SignalingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Client SENDs to /app/signal.send. senderId in the incoming payload
     * is ignored — the verified sender always comes from the STOMP
     * session's Principal (StompAuthChannelInterceptor, set on CONNECT),
     * same identity-trust rule as ChatController's /chat and
     * /society.chat. Without this, a client could forge OFFER/ANSWER/
     * ICE_CANDIDATE signals claiming to be any other user — spoofing a
     * call invite "from" someone else, for instance.
     *
     * type/sdp/candidate are relayed through unexamined — this controller
     * doesn't interpret signaling semantics, it just gets the payload to
     * the right session via convertAndSendToUser, resolving to
     * /user/{receiverId}/queue/signals.
     */
    @MessageMapping("/signal.send")
    public void relaySignal(WebRTCSignalDTO incoming, Principal principal) {
        UUID senderId = StompPrincipalResolver.resolveUserId(principal);

        WebRTCSignalDTO verified = WebRTCSignalDTO.builder()
                .type(incoming.getType())
                .senderId(senderId)
                .receiverId(incoming.getReceiverId())
                .sdp(incoming.getSdp())
                .candidate(incoming.getCandidate())
                .build();

        messagingTemplate.convertAndSendToUser(
                verified.getReceiverId().toString(),
                "/queue/signals",
                verified
        );
    }
}
