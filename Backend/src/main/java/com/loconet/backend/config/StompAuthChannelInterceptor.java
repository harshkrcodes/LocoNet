// File: Backend/src/main/java/com/loconet/backend/config/StompAuthChannelInterceptor.java
package com.loconet.backend.config;

import com.loconet.backend.security.JwtUtil;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.UUID;

/**
 * Replaces Phase 5's UserHandshakeHandler/StompPrincipal (which trusted an
 * unverified ?userId= query param). This runs on every inbound STOMP
 * frame, but only acts on CONNECT: it reads the JWT from the STOMP
 * "Authorization" header (native STOMP header, not an HTTP one — the
 * client sets it when opening the STOMP CONNECT frame, which it CAN
 * customize, unlike the WebSocket HTTP handshake itself), validates it,
 * and assigns the session's Principal from the token's subject claim.
 *
 * A missing/invalid token throws, which Spring converts into a STOMP
 * ERROR frame and closes the connection — the client never reaches
 * SUBSCRIBE/SEND without a valid token.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public StompAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractBearerToken(accessor);

            if (token == null) {
                throw new MessagingException("Missing or malformed Authorization header on STOMP CONNECT");
            }

            if (!jwtUtil.isTokenValid(token)) {
                throw new MessagingException("Invalid or expired JWT on STOMP CONNECT");
            }

            UUID userId = jwtUtil.extractUserId(token);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

            // This is what makes convertAndSendToUser(receiverId, ...) in
            // ChatController route correctly — the session's user is now
            // the verified JWT subject, not a client-asserted query param.
            accessor.setUser(authToken);
        }

        return message;
    }

    @Nullable
    private String extractBearerToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
