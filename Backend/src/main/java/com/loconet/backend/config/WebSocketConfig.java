// File: Backend/src/main/java/com/loconet/backend/config/WebSocketConfig.java
package com.loconet.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new UserHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/queue" is the only real broker destination prefix. "/user" is
        // NOT registered here — it's a routing prefix intercepted by
        // Spring's UserDestinationMessageHandler and rewritten to a
        // session-specific "/queue/..." destination before it ever reaches
        // the broker. Passing "/user" to enableSimpleBroker would just
        // create a dead destination nothing publishes to.
        registry.enableSimpleBroker("/queue");

        // Client SEND frames go to /app/** (e.g. ChatController's
        // @MessageMapping("/chat") is reached via /app/chat).
        registry.setApplicationDestinationPrefixes("/app");

        // Matches Spring's default, set explicitly since
        // convertAndSendToUser(...) depends on it and UserHandshakeHandler
        // exists specifically to make this prefix resolvable per-session.
        registry.setUserDestinationPrefix("/user");
    }
}
