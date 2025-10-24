package com.example.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.enums.NotificationType;
import com.example.demo.events.LowStockEvent;
import com.example.demo.events.StockDepletedEvent;
import com.example.demo.events.StockReservationFailedEvent;
import com.example.demo.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockEventsListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "low-stock", groupId = "notification-service-group")
    public void handleLowStock(LowStockEvent event) {
        log.info("📬 Received LowStockEvent for productId: {}", event.getProductId());
        
        try {
            // Notification SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.LOW_STOCK)
                    .title("⚠️ Alerte stock faible")
                    .message(String.format("Stock faible pour '%s'. Stock actuel: %d unités (seuil: %d). Réapprovisionnez rapidement!", 
                            event.getProductName(), event.getCurrentStock(), event.getThreshold()))
                    .metadata(String.format("{\"productId\":%d,\"productName\":\"%s\",\"currentStock\":%d,\"threshold\":%d}", 
                            event.getProductId(), event.getProductName(), event.getCurrentStock(), event.getThreshold()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Seller notification sent for low stock product: {}", event.getProductId());
        } catch (Exception e) {
            log.error("❌ Error handling LowStockEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "stock-depleted", groupId = "notification-service-group")
    public void handleStockDepleted(StockDepletedEvent event) {
        log.info("📬 Received StockDepletedEvent for productId: {}", event.getProductId());
        
        try {
            // Notification SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.STOCK_DEPLETED)
                    .title("🚨 Rupture de stock")
                    .message(String.format("Rupture de stock pour '%s'. Le produit n'est plus disponible à la vente. Réapprovisionnez d'urgence!", 
                            event.getProductName()))
                    .metadata(String.format("{\"productId\":%d,\"productName\":\"%s\"}", 
                            event.getProductId(), event.getProductName()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Seller notification sent for depleted stock: {}", event.getProductId());
        } catch (Exception e) {
            log.error("❌ Error handling StockDepletedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "stock-reservation-failed", groupId = "notification-service-group")
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        log.info("📬 Received StockReservationFailedEvent for orderId: {}", event.getOrderId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.STOCK_RESERVATION_FAILED)
                    .title("❌ Produit indisponible")
                    .message(String.format("Le produit '%s' n'est plus disponible en quantité suffisante. Votre commande #%d a été annulée.", 
                            event.getProductName(), event.getOrderId()))
                    .metadata(String.format("{\"orderId\":%d,\"productId\":%d,\"productName\":\"%s\"}", 
                            event.getOrderId(), event.getProductId(), event.getProductName()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            log.info("✅ Client notification sent for failed stock reservation: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error handling StockReservationFailedEvent: {}", e.getMessage(), e);
        }
    }
}