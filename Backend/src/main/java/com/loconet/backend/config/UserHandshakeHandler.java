// File: Backend/src/main/java/com/loconet/backend/config/UserHandshakeHandler.java
package com.loconet.backend.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves the STOMP session's Principal from a `userId` query parameter on
 * the handshake URL, e.g. ws://host/ws?userId=<uuid>.
 *
 * This is what makes SimpMessagingTemplate.convertAndSendToUser(receiverId,
 * ...) route to the right session — without a Principal, Spring has no way
 * to associate a WebSocket session with a user id.
 *
 * IMPORTANT: this trusts a client-supplied query parameter as identity.
 * That is NOT authentication — anyone can connect claiming to be any
 * userId. It's an interim measure until Phase 6 adds real auth, at which
 * point determineUser() should pull the Principal from the authenticated
 * session instead, and this class (plus StompPrincipal) can be deleted.
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                       WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
        String userId = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("userId");

        if (!StringUtils.hasText(userId)) {
            // Falls back to a random id so the handshake itself doesn't
            // fail, but a session like this can never be reached by
            // convertAndSendToUser — a missing userId is a client bug,
            // not something to silently paper over further than this.
            userId = UUID.randomUUID().toString();
        }

        return new StompPrincipal(userId);
    }
}
