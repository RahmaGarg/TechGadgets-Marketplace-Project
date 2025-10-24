package com.example.demo.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.NotificationRequest;
import com.example.demo.enums.NotificationType;
import com.example.demo.events.PaymentFailedEvent;
import com.example.demo.events.PaymentProcessedEvent;
import com.example.demo.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventsListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-processed", groupId = "notification-service-group")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("📬 Received PaymentProcessedEvent for paymentId: {}", event.getPaymentId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.PAYMENT_SUCCESS)
                    .title("✅ Paiement confirmé")
                    .message(String.format("Paiement de %.2f DT confirmé via %s. Votre commande #%d est en cours de préparation.", 
                            event.getAmount(), event.getPaymentMethod(), event.getOrderId()))
                    .metadata(String.format("{\"paymentId\":%d,\"orderId\":%d,\"amount\":%.2f,\"method\":\"%s\"}", 
                            event.getPaymentId(), event.getOrderId(), event.getAmount(), event.getPaymentMethod()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            log.info("✅ Client notification sent for successful payment: {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Error handling PaymentProcessedEvent: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "notification-service-group")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("📬 Received PaymentFailedEvent for paymentId: {}", event.getPaymentId());
        
        try {
            // Notification CLIENT
            NotificationRequest clientNotif = NotificationRequest.builder()
                    .userId(event.getClientId())
                    .userRole("CLIENT")
                    .type(NotificationType.PAYMENT_FAILED)
                    .title("❌ Échec du paiement")
                    .message(String.format("Le paiement de la commande #%d a échoué. Raison: %s. Veuillez réessayer.", 
                            event.getOrderId(), event.getFailureReason()))
                    .metadata(String.format("{\"paymentId\":%d,\"orderId\":%d,\"reason\":\"%s\"}", 
                            event.getPaymentId(), event.getOrderId(), event.getFailureReason()))
                    .build();
            
            notificationService.createNotification(clientNotif);
            
            // Notification ADMIN (pour monitoring fraude)
            NotificationRequest adminNotif = NotificationRequest.builder()
                    .userId(1L) // Admin ID
                    .userRole("ADMIN")
                    .type(NotificationType.PAYMENT_FAILED_ADMIN)
                    .title("⚠️ Paiement échoué")
                    .message(String.format("Paiement échoué pour commande #%d. Client: %d. Raison: %s", 
                            event.getOrderId(), event.getClientId(), event.getFailureReason()))
                    .metadata(String.format("{\"paymentId\":%d,\"orderId\":%d,\"clientId\":%d,\"reason\":\"%s\"}", 
                            event.getPaymentId(), event.getOrderId(), event.getClientId(), event.getFailureReason()))
                    .build();
            
            notificationService.createNotification(adminNotif);
            log.info("✅ Notifications sent for failed payment: {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("❌ Error handling PaymentFailedEvent: {}", e.getMessage(), e);
        }
    }
}