package com.example.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.enums.NotificationType;
import com.example.demo.events.SellerRegisteredEvent;
import com.example.demo.events.UserRegisteredEvent;
import com.example.demo.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventsListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-registered-topic", groupId = "notification-service-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("📬 Received UserRegisteredEvent for userId: {}", event.getUserId());
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .userRole("CLIENT")
                    .type(NotificationType.CLIENT_ACCOUNT_CREATED)  // ✅ Changed
                    .title("Bienvenue sur TechGadgets!")
                    .message(String.format("Bienvenue %s %s! Votre compte client a été créé avec succès. Explorez nos produits tech dès maintenant!", 
                            event.getFirstName(), event.getLastName()))
                    .metadata(String.format("{\"email\":\"%s\"}", event.getEmail()))
                    .build();
            
            notificationService.createNotification(request);
            log.info("✅ Notification sent to user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Error handling UserRegisteredEvent: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "seller-registered-topic", groupId = "notification-service-group")
    public void handleSellerRegistered(SellerRegisteredEvent event) {
        log.info("📬 Received SellerRegisteredEvent for userId: {}", event.getUserId());
        
        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .userRole("SELLER")
                    .type(NotificationType.SELLER_ACCOUNT_CREATED)  // ✅ Changed
                    .title("Compte vendeur activé")
                    .message(String.format("Bienvenue %s! Votre boutique est maintenant active. Vous pouvez commencer à ajouter vos produits.", 
                            event.getName()))
                    .metadata(String.format("{\"name\":\"%s\",\"email\":\"%s\",\"registeredAt\":\"%s\"}", 
                            event.getName(), event.getEmail(), event.getRegisteredAt()))
                    .build();
            
            notificationService.createNotification(request);
            log.info("✅ Notification sent to seller: {}", event.getUserId());
        } catch (Exception e) {
            log.error("❌ Error handling SellerRegisteredEvent: {}", e.getMessage(), e);
        }
    }}