package com.example.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.enums.NotificationType;
import com.example.demo.events.ReviewCreatedEvent;
import com.example.demo.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewEventsListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "review-created", groupId = "notification-service-group")
    public void handleReviewCreated(ReviewCreatedEvent event) {
        log.info("📬 Received ReviewCreatedEvent for reviewId: {}, productId: {}, sellerId: {}", 
                event.getReviewId(), event.getProductId(), event.getSellerId());
        
        try {
            // Créer les étoiles pour le rating
            String ratingStars = "⭐".repeat(event.getRating());
            
            // Notification pour le VENDEUR
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId()) // ID du vendeur
                    .userRole("SELLER")
                    .type(NotificationType.NEW_REVIEW)
                    .title("Nouvel avis sur votre produit")
                    .message(String.format("Votre produit a reçu %d étoiles %s", 
                         event.getRating(), 
                         ratingStars))
                    .metadata(String.format("{\"productId\": %d, \"reviewId\": %d, \"rating\": %d}", 
                         event.getProductId(), 
                         event.getReviewId(), 
                         event.getRating()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Seller notification sent for product: {}, review: {}", 
                    event.getProductId(), event.getReviewId());
            
        } catch (Exception e) {
            log.error("❌ Error processing ReviewCreatedEvent for review {}: {}", 
                     event.getReviewId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process review creation notification", e);
        }
    }
}