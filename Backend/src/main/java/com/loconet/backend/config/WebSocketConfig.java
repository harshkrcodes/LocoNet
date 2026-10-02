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
                .setHandshakeHandler(new UserHandshakeHandler())
                .withSockJS(); // Humara connection fix zinda hai!
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/queue" is for 1-on-1 chats.
        // "/topic" is added for Society/Group broadcasts.
        registry.enableSimpleBroker("/queue", "/topic");

        // Client SEND frames go to /app/**
        registry.setApplicationDestinationPrefixes("/app");

        // Matches Spring's default for session-specific messaging
        registry.setUserDestinationPrefix("/user");
    }
}