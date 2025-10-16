package com.example.demo.services;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.config.KonnectConfig;
import com.example.demo.dtos.PaymentRequest;
import com.example.demo.dtos.PaymentResponse;
import com.example.demo.entities.Payment;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.PaymentRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class KonnectService {
    
    private final KonnectConfig konnectConfig;
    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    
    @Value("${server.public-url}")
    private String publicUrl;
    
    public KonnectService(KonnectConfig konnectConfig, PaymentRepository paymentRepository) {
        this.konnectConfig = konnectConfig;
        this.paymentRepository = paymentRepository;
        this.restTemplate = new RestTemplate();
    }
    
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            String url = konnectConfig.getBaseUrl() + "/api/v2/payments/init-payment";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", konnectConfig.getApiKey());
            
            // Convert amount to millimes (1 TND = 1000 millimes)
            long amountInMillimes = (long) (request.getAmount() * 1000);
            
            // Build webhook URL
            String webhookUrl = publicUrl + "/webhook/konnect";
            String successUrl = request.getSuccessUrl() != null 
                ? request.getSuccessUrl() 
                : publicUrl + "/payment/konnect/success";
            String failUrl = request.getFailUrl() != null 
                ? request.getFailUrl() 
                : publicUrl + "/payment/konnect/fail";
            
            Map<String, Object> body = new HashMap<>();
            body.put("receiverWalletId", konnectConfig.getWalletId());
            body.put("amount", amountInMillimes);
            body.put("token", "TND");
            body.put("type", "immediate");
            body.put("description", request.getDescription());
            body.put("acceptedPaymentMethods", Arrays.asList("wallet", "bank_card", "e-DINAR"));
            body.put("lifespan", 15); // 15 minutes
            body.put("checkoutForm", true);
            body.put("addPaymentFeesToAmount", true);
            body.put("webhook", webhookUrl);
            body.put("silentWebhook", true);
            body.put("successUrl", successUrl);
            body.put("failUrl", failUrl);
            
            // Add customer info if available
            if (request.getCustomerEmail() != null) {
                body.put("email", request.getCustomerEmail());
            }
            if (request.getCustomerName() != null) {
                String[] nameParts = request.getCustomerName().split(" ", 2);
                body.put("firstName", nameParts[0]);
                if (nameParts.length > 1) {
                    body.put("lastName", nameParts[1]);
                }
            }
            
            // Generate unique order ID
            String orderId = UUID.randomUUID().toString();
            body.put("orderId", orderId);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            log.info("Creating Konnect payment with webhook: {}", webhookUrl);
            ResponseEntity<KonnectInitResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                KonnectInitResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return processKonnectResponse(response.getBody(), request, orderId);
            }
            
            throw new RuntimeException("Failed to create Konnect payment");
            
        } catch (Exception e) {
            log.error("Error creating Konnect payment", e);
            throw new RuntimeException("Konnect payment creation failed: " + e.getMessage());
        }
    }
    
    private PaymentResponse processKonnectResponse(KonnectInitResponse response, 
                                                   PaymentRequest request, 
                                                   String orderId) {
        String paymentRef = response.getPaymentRef();
        String payUrl = response.getPayUrl();
        
        Payment payment = Payment.builder()
            .paymentId(orderId)
            .method(PaymentMethod.KONNECT)
            .status(PaymentStatus.PENDING)
            .amount(request.getAmount())
            .currency("TND")
            .description(request.getDescription())
            .customerEmail(request.getCustomerEmail())
            .externalPaymentId(paymentRef)
            .build();
        
        paymentRepository.save(payment);
        
        return PaymentResponse.builder()
            .paymentId(orderId)
            .status(PaymentStatus.PENDING)
            .approvalUrl(payUrl)
            .externalPaymentId(paymentRef)
            .gateway("KONNECT")
            .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
            .message("Payment created successfully. Redirect customer to approval URL.")
            .build();
    }
    
    public Payment verifyPayment(String paymentRef) {
        try {
            String url = konnectConfig.getBaseUrl() + "/api/v2/payments/" + paymentRef;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", konnectConfig.getApiKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("Verifying Konnect payment: {}", paymentRef);
            ResponseEntity<KonnectStatusResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KonnectStatusResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                KonnectStatusResponse statusResponse = response.getBody();
                
                Payment payment = paymentRepository.findByExternalPaymentId(paymentRef)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
                
                // Update payment status based on Konnect response
                String status = statusResponse.getStatus();
                if ("completed".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status)) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                } else if ("failed".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status)) {
                    payment.setStatus(PaymentStatus.FAILED);
                } else {
                    payment.setStatus(PaymentStatus.PENDING);
                }
                
                return paymentRepository.save(payment);
            }
            
            throw new RuntimeException("Failed to verify Konnect payment");
            
        } catch (Exception e) {
            log.error("Error verifying Konnect payment", e);
            throw new RuntimeException("Konnect verification failed: " + e.getMessage());
        }
    }
    
    @Data
    public static class KonnectInitResponse {
        private String payUrl;
        private String paymentRef;
    }
    
    @Data
    public static class KonnectStatusResponse {
        private String paymentRef;
        private String orderId;
        private String status;
        private Long amount;
        private String receiverWalletId;
        private String createdAt;
    }
}