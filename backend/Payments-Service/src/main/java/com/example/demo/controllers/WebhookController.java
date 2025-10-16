package com.example.demo.controllers;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.PaymentRequest;
import com.example.demo.dtos.PaymentResponse;
import com.example.demo.entities.Payment;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.services.KonnectService;
import com.example.demo.services.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@Slf4j
public class WebhookController {

    private final PaymentRepository paymentRepository;
    private final KonnectService konnectService;

    public WebhookController(PaymentRepository paymentRepository, KonnectService konnectService) {
        this.paymentRepository = paymentRepository;
        this.konnectService = konnectService;
    }

    /**
     * Konnect webhook - receives GET request with payment_ref as query parameter
     * Example: GET /webhook/konnect?payment_ref=5f9498735289e405fc7c18ac
     */
    @GetMapping("/konnect")
    public ResponseEntity<String> handleKonnectWebhook(@RequestParam("payment_ref") String paymentRef) {
        try {
            log.info("Received Konnect webhook for payment_ref: {}", paymentRef);
            
            // Verify payment status with Konnect API
            Payment payment = konnectService.verifyPayment(paymentRef);
            
            log.info("Konnect payment {} status updated to: {}", paymentRef, payment.getStatus());
            
            return ResponseEntity.ok("Webhook processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing Konnect webhook for payment_ref: {}", paymentRef, e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }

    /**
     * PayPal webhook (if needed in the future)
     */
    @PostMapping("/paypal")
    public ResponseEntity<String> handlePayPalWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received PayPal webhook: {}", payload);
            
            String eventType = (String) payload.get("event_type");
            
            if ("PAYMENT.CAPTURE.COMPLETED".equals(eventType)) {
                handlePaymentCompleted(payload);
            } else if ("PAYMENT.CAPTURE.DENIED".equals(eventType)) {
                handlePaymentFailed(payload);
            }
            
            return ResponseEntity.ok("Webhook received");
            
        } catch (Exception e) {
            log.error("Error processing PayPal webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }

    private void handlePaymentCompleted(Map<String, Object> data) {
        String paymentId = (String) data.get("payment_id");
        paymentRepository.findByExternalPaymentId(paymentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            log.info("Payment {} completed successfully", paymentId);
        });
    }

    private void handlePaymentFailed(Map<String, Object> data) {
        String paymentId = (String) data.get("payment_id");
        paymentRepository.findByExternalPaymentId(paymentId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.info("Payment {} failed", paymentId);
        });
    }
}