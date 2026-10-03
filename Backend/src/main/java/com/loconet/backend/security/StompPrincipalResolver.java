// File: Backend/src/main/java/com/loconet/backend/security/StompPrincipalResolver.java
//
// Extracted from ChatController's private resolveUserId() so
// SignalingController doesn't need its own copy of identity-trust logic.
// Every current and future STOMP @MessageMapping controller should use
// this rather than re-implementing the cast — it's the one place that
// knows how StompAuthChannelInterceptor's accessor.setUser(...) call
// shows up on the other end.
package com.loconet.backend.security;

import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.UUID;

public final class StompPrincipalResolver {

    private StompPrincipalResolver() {
    }

    /**
     * Principal here is the Authentication StompAuthChannelInterceptor set
     * via accessor.setUser() on CONNECT — its principal object is the raw
     * UUID (same pattern JwtAuthFilter uses for REST requests). Cast
     * rather than parsing principal.getName() as a string: more direct,
     * and doesn't depend on AbstractAuthenticationToken's
     * getName()-falls-back-to-toString() behavior staying the same.
     */
    public static UUID resolveUserId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UUID userId) {
            return userId;
        }
        throw new IllegalStateException(
                "STOMP session has no authenticated user id — StompAuthChannelInterceptor "
                        + "should have rejected this connection before it reached here");
    }
}
