package com.example.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.enums.NotificationType;
import com.example.demo.events.OrderCancelledEvent;
import com.example.demo.events.OrderConfirmedEvent;
import com.example.demo.events.OrderCreatedEvent;
import com.example.demo.events.OrderDeliveredEvent;
import com.example.demo.events.OrderShippedEvent;
import com.example.demo.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-created", groupId = "notification-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📬 Received OrderCreatedEvent for orderId: {}", event.getOrderId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.ORDER_CREATED)
                    .title("🛒 Commande créée")
                    .message(String.format("Votre commande #%d a été créée avec succès. Montant total: %.2f DT. En attente de paiement.", 
                            event.getOrderId(), event.getTotalAmount()))
                    .metadata(String.format("{\"orderId\":%d,\"totalAmount\":%.2f,\"itemCount\":%d}", 
                            event.getOrderId(), event.getTotalAmount(), event.getItems().size()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            log.info("✅ Client notification sent for order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error handling OrderCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-confirmed", groupId = "notification-service-group")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("📬 Received OrderConfirmedEvent for orderId: {}", event.getOrderId());
        
        try {
            // Notification SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.NEW_ORDER)
                    .title("🎉 Nouvelle commande confirmée")
                    .message(String.format("Commande #%d confirmée et payée. Préparez l'expédition rapidement!", 
                            event.getOrderId()))
                    .metadata(String.format("{\"orderId\":%d,\"clientId\":%d}", 
                            event.getOrderId(), event.getClientId()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Seller notification sent for confirmed order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error handling OrderConfirmedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-shipped", groupId = "notification-service-group")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("📬 Received OrderShippedEvent for orderId: {}", event.getOrderId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.ORDER_SHIPPED)
                    .title("📦 Colis expédié")
                    .message(String.format("Votre commande #%d a été expédiée! N° de suivi: %s. Livraison estimée: %s", 
                            event.getOrderId(), 
                            event.getTrackingNumber(),
                            event.getEstimatedDelivery() != null ? event.getEstimatedDelivery().toString() : "Non spécifiée"))
                    .metadata(String.format("{\"orderId\":%d,\"trackingNumber\":\"%s\"}", 
                            event.getOrderId(), event.getTrackingNumber()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            log.info("✅ Client notification sent for shipped order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error handling OrderShippedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-delivered", groupId = "notification-service-group")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("📬 Received OrderDeliveredEvent for orderId: {}", event.getOrderId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.ORDER_DELIVERED)
                    .title("✅ Colis livré")
                    .message(String.format("Commande #%d livrée avec succès! N'oubliez pas de laisser un avis sur les produits.", 
                            event.getOrderId()))
                    .metadata(String.format("{\"orderId\":%d}", event.getOrderId()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            
            // Notification SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.ORDER_DELIVERED_SELLER)
                    .title("✅ Commande livrée")
                    .message(String.format("Commande #%d livrée avec succès au client.", 
                            event.getOrderId()))
                    .metadata(String.format("{\"orderId\":%d,\"clientId\":%d}", 
                            event.getOrderId(), event.getClientId()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Notifications sent for delivered order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error handling OrderDeliveredEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "notification-service-group")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("📬 Received OrderCancelledEvent for orderId: {}", event.getOrderId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.ORDER_CANCELLED)
                    .title("❌ Commande annulée")
                    .message(String.format("Commande #%d annulée. Raison: %s", 
                            event.getOrderId(), event.getCancellationReason()))
                    .metadata(String.format("{\"orderId\":%d,\"reason\":\"%s\"}", 
                            event.getOrderId(), event.getCancellationReason()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            
            // Notification SELLER
            NotificationRequest sellerNotif = NotificationRequest.builder()
                    .userId(event.getSellerId())
                    .userRole("SELLER")
                    .type(NotificationType.ORDER_CANCELLED_BY_CLIENT)
                    .title("❌ Commande annulée")
                    .message(String.format("Commande #%d annulée par le client. Raison: %s", 
                            event.getOrderId(), event.getCancellationReason()))
                    .metadata(String.format("{\"orderId\":%d,\"reason\":\"%s\",\"clientId\":%d}", 
                            event.getOrderId(), event.getCancellationReason(), event.getClientId()))
                    .build();
            
            notificationService.createNotification(sellerNotif);
            log.info("✅ Notifications sent for cancelled order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("❌ Error handling OrderCancelledEvent: {}", e.getMessage(), e);
        }
    }
}