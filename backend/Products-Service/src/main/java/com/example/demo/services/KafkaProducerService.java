package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.events.ProductApprovedEvent;
import com.example.demo.events.ProductCreatedEvent;
import com.example.demo.events.ProductDeletedEvent;
import com.example.demo.events.ProductRejectedEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendProductCreatedEvent(ProductCreatedEvent event) {
        log.info("📤 Sending ProductCreatedEvent for productId: {}", event.getProductId());
        kafkaTemplate.send("product-created", event);
        log.info("✅ ProductCreatedEvent sent successfully");
    }

    public void sendProductApprovedEvent(ProductApprovedEvent event) {
        log.info("📤 Sending ProductApprovedEvent for productId: {}", event.getProductId());
        kafkaTemplate.send("product-approved", event);
        log.info("✅ ProductApprovedEvent sent successfully");
    }

    public void sendProductRejectedEvent(ProductRejectedEvent event) {
        log.info("📤 Sending ProductRejectedEvent for productId: {}", event.getProductId());
        kafkaTemplate.send("product-rejected", event);
        log.info("✅ ProductRejectedEvent sent successfully");
    }

    public void sendProductDeletedEvent(ProductDeletedEvent event) {
        log.info("📤 Sending ProductDeletedEvent for productId: {}", event.getProductId());
        kafkaTemplate.send("product-deleted", event);
        log.info("✅ ProductDeletedEvent sent successfully");
    }
}