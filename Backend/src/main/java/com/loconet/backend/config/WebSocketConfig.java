// File: Backend/src/main/java/com/loconet/backend/config/WebSocketConfig.java
// UPDATED for Phase 6 — removed Phase 5's UserHandshakeHandler (insecure,
// trusted a raw ?userId= query param) and wired in
// StompAuthChannelInterceptor instead, which validates a real JWT on the
// STOMP CONNECT frame and sets the session's Principal from it.
package com.loconet.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public WebSocketConfig(StompAuthChannelInterceptor stompAuthChannelInterceptor) {
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // No setHandshakeHandler(...) anymore — session identity now comes
        // from the validated JWT in StompAuthChannelInterceptor, not the
        // handshake URL. SecurityConfig permits this HTTP endpoint openly;
        // see its comment for why that's correct, not a gap.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Real broker destination prefixes. "/user" is deliberately NOT
        // registered here — see the original Phase 5 note: it's a routing
        // prefix handled by setUserDestinationPrefix below, not a broker
        // destination.
        registry.enableSimpleBroker("/queue", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
