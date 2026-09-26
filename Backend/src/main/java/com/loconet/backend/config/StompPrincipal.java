// File: Backend/src/main/java/com/loconet/backend/config/StompPrincipal.java
package com.loconet.backend.config;

import java.security.Principal;

/**
 * Minimal Principal wrapping a userId string. Exists purely so STOMP user
 * destinations (/user/{userId}/queue/...) can resolve a session's identity
 * without full Spring Security. This is an interim measure for Phase 5 —
 * delete it once Phase 6 auth introduces a real Principal.
 */
public class StompPrincipal implements Principal {

    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
