package com.example.demo.kafka.consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.dtos.WebSocketNotification;
import com.example.demo.enums.NotificationType;
import com.example.demo.events.ProductApprovedEvent;
import com.example.demo.events.ProductCreatedEvent;
import com.example.demo.events.ProductRejectedEvent;
import com.example.demo.service.NotificationService;
import com.example.demo.service.WebSocketService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventsListener {

    private final NotificationService notificationService;
    private final WebSocketService webSocketService;


    @KafkaListener(topics = "product-created", groupId = "notification-service-group")
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("📬 Received ProductCreatedEvent for productId: {}", event.getProductId());
        
        try {
            // Créer la notification WebSocket
            WebSocketNotification wsNotif = WebSocketNotification.builder()
                    .type(NotificationType.NEW_PRODUCT_PENDING)
                    .title("Nouveau produit à valider")
                    .message(String.format(
                        "Produit '%s' soumis par le vendeur ID %d", 
                        event.getProductName(), 
                        event.getSellerId()
                    ))
                    .metadata(String.format(
                        "{\"productId\":%d,\"sellerId\":%d,\"productName\":\"%s\",\"action\":\"MODERATE_PRODUCT\"}", 
                        event.getProductId(), 
                        event.getSellerId(), 
                        event.getProductName()
                    ))
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Broadcast à tous les admins connectés
            webSocketService.broadcastToRole("ADMIN", wsNotif);
            
            log.info("✅ Product creation broadcast to ADMIN role for product: {}", event.getProductId());
            
        } catch (Exception e) {
            log.error("❌ Error handling ProductCreatedEvent: {}", e.getMessage(), e);
        }
    }
    @KafkaListener(topics = "product-approved", groupId = "notification-service-group")
    public void handleProductApproved(ProductApprovedEvent event) {
        log.info("📬 Received ProductApprovedEvent for productId: {}", event.getProductId());
        
        try {
            // Notification pour SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.PRODUCT_APPROVED)
                    .title("✅ Produit approuvé")
                    .message(String.format("Félicitations! Votre produit '%s' a été approuvé et est maintenant visible publiquement.", 
                            event.getProductName()))
                    .metadata(String.format("{\"productId\":%d,\"productName\":\"%s\"}", 
                            event.getProductId(), event.getProductName()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Seller notification sent for approved product: {}", event.getProductId());
        } catch (Exception e) {
            log.error("❌ Error handling ProductApprovedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "product-rejected", groupId = "notification-service-group")
    public void handleProductRejected(ProductRejectedEvent event) {
        log.info("📬 Received ProductRejectedEvent for productId: {}", event.getProductId());
        
        try {
            // Notification pour SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.PRODUCT_REJECTED)
                    .title("❌ Produit rejeté")
                    .message(String.format("Votre produit '%s' a été rejeté. Raison: %s", 
                            event.getProductName(), event.getRejectionReason()))
                    .metadata(String.format("{\"productId\":%d,\"productName\":\"%s\",\"reason\":\"%s\"}", 
                            event.getProductId(), event.getProductName(), event.getRejectionReason()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Seller notification sent for rejected product: {}", event.getProductId());
        } catch (Exception e) {
            log.error("❌ Error handling ProductRejectedEvent: {}", e.getMessage(), e);
        }
    }
}