package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Value("${websocket.allowed-origins}")
    private String[] allowedOrigins;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefix pour les messages envoyés depuis le serveur vers le client
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefix pour les messages envoyés depuis le client vers le serveur
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefix pour les messages destinés à un utilisateur spécifique
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket accessible par les clients
        registry.addEndpoint("/ws/notifications")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS(); // Fallback pour navigateurs sans WebSocket
    }
}