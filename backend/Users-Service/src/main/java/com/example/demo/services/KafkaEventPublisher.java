package com.example.demo.services;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.events.SellerRegisteredEvent;
import com.example.demo.events.UserProfileCompletedEvent;
import com.example.demo.events.UserRegisteredEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send("user-registered-topic", event);
            log.info("Published UserRegisteredEvent for user: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish UserRegisteredEvent", e);
        }
    }

    public void publishSellerRegisteredEvent(SellerRegisteredEvent event) {
        try {
            kafkaTemplate.send("seller-registered-topic", event);
            log.info("Published SellerRegisteredEvent for seller: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish SellerRegisteredEvent", e);
        }
    }

    public void publishUserProfileCompletedEvent(UserProfileCompletedEvent event) {
        try {
            kafkaTemplate.send("user-profile-completed-topic", event);
            log.info("Published UserProfileCompletedEvent for user: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish UserProfileCompletedEvent", e);
        }
    }
}