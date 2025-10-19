package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.WebSocketNotification;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Envoyer une notification à un utilisateur spécifique
     * Le client doit être abonné à /user/queue/notifications
     */
    public void sendNotificationToUser(Long userId, WebSocketNotification notification) {
        String destination = "/user/" + userId + "/queue/notifications";
        log.info("Sending notification to destination: {}", destination);
        
        try {
            messagingTemplate.convertAndSend(destination, notification);
            log.info("Notification sent successfully to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Broadcast à tous les utilisateurs d'un rôle (ex: tous les admins)
     * Le client doit être abonné à /topic/notifications/{role}
     */
    public void broadcastToRole(String role, WebSocketNotification notification) {
        String destination = "/topic/notifications/" + role.toLowerCase();
        log.info("Broadcasting notification to role: {}", role);
        
        try {
            messagingTemplate.convertAndSend(destination, notification);
            log.info("Notification broadcasted successfully to role: {}", role);
        } catch (Exception e) {
            log.error("Failed to broadcast to role {}: {}", role, e.getMessage());
        }
    }
}