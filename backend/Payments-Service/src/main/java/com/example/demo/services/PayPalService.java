package com.example.demo.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.demo.config.PayPalConfig;
import com.example.demo.dtos.PaymentRequest;
import com.example.demo.dtos.PaymentResponse;
import com.example.demo.entities.Payment;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.PaymentRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PayPalService {
    
    private final PayPalConfig payPalConfig;
    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    
    private String accessToken;
    private Instant tokenExpiry;
    
    public PayPalService(PayPalConfig payPalConfig, PaymentRepository paymentRepository) {
        this.payPalConfig = payPalConfig;
        this.paymentRepository = paymentRepository;
        this.restTemplate = new RestTemplate();
    }
    
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            log.info("Getting PayPal access token...");
            String accessToken = getAccessToken();
            log.info("Access token obtained successfully");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            Map<String, Object> paymentRequest = createPaymentRequestBody(request);
            log.info("Payment request body: {}", paymentRequest);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
            
            String apiUrl = payPalConfig.getBaseUrl() + "/v2/checkout/orders";
            log.info("Calling PayPal API: {}", apiUrl);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            log.info("PayPal API response status: {}", response.getStatusCode());
            log.info("PayPal API response body: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return processPayPalResponse(response.getBody(), request);
            }
            
            throw new RuntimeException("Failed to create PayPal payment. Status: " + response.getStatusCode());
            
        } catch (Exception e) {
            log.error("Error creating PayPal payment", e);
            throw new RuntimeException("PayPal payment creation failed: " + e.getMessage());
        }
    }
    private Map<String, Object> createPaymentRequestBody(PaymentRequest request) {
        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", request.getCurrency());
        
        // FIX: Use US locale for number formatting to ensure decimal point
        amount.put("value", String.format(Locale.US, "%.2f", request.getAmount()));
        
        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", amount);
        purchaseUnit.put("description", request.getDescription());
        
        Map<String, Object> applicationContext = new HashMap<>();
        applicationContext.put("return_url", payPalConfig.getReturnUrl());
        applicationContext.put("cancel_url", payPalConfig.getCancelUrl());
        applicationContext.put("brand_name", "Your Company");
        
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("intent", "CAPTURE");
        paymentRequest.put("purchase_units", Collections.singletonList(purchaseUnit));
        paymentRequest.put("application_context", applicationContext);
        
        return paymentRequest;
    }
    
    private PaymentResponse processPayPalResponse(Map<String, Object> response, PaymentRequest request) {
        String paymentId = (String) response.get("id");
        String status = (String) response.get("status");
        
        List<Map<String, Object>> links = (List<Map<String, Object>>) response.get("links");
        String approvalUrl = links.stream()
            .filter(link -> "approve".equals(link.get("rel")))
            .map(link -> (String) link.get("href"))
            .findFirst()
            .orElse(null);
        
        Payment payment = Payment.builder()
            .paymentId(UUID.randomUUID().toString())
            .method(PaymentMethod.PAYPAL)
            .status(PaymentStatus.PENDING)
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .description(request.getDescription())
            .customerEmail(request.getCustomerEmail())
            .externalPaymentId(paymentId)
            .build();
        
        paymentRepository.save(payment);
        
        return PaymentResponse.builder()
            .paymentId(payment.getPaymentId())
            .status(PaymentStatus.PENDING)
            .approvalUrl(approvalUrl)
            .externalPaymentId(paymentId)
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();
    }
    
    public Payment capturePayment(String orderId) {
        try {
            String accessToken = getAccessToken();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                payPalConfig.getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Payment payment = paymentRepository.findByExternalPaymentId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            if (response.getStatusCode().is2xxSuccessful()) {
                payment.setStatus(PaymentStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
            
            return paymentRepository.save(payment);
            
        } catch (Exception e) {
            log.error("Error capturing PayPal payment", e);
            throw new RuntimeException("Payment capture failed");
        }
    }
    
    private String getAccessToken() {
        if (accessToken != null && tokenExpiry != null && tokenExpiry.isAfter(Instant.now())) {
            return accessToken;
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(payPalConfig.getClientId(), payPalConfig.getClientSecret());
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            payPalConfig.getBaseUrl() + "/v1/oauth2/token",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            this.accessToken = (String) response.getBody().get("access_token");
            
            // FIX: Handle both Integer and Long types for expires_in
            Object expiresInObj = response.getBody().get("expires_in");
            long expiresIn;
            
            if (expiresInObj instanceof Integer) {
                expiresIn = ((Integer) expiresInObj).longValue();
            } else if (expiresInObj instanceof Long) {
                expiresIn = (Long) expiresInObj;
            } else {
                // Default to 1 hour if type is unexpected
                expiresIn = 3600L;
                log.warn("Unexpected expires_in type: {}, using default 3600 seconds", expiresInObj.getClass().getSimpleName());
            }
            
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn - 60); // 60 seconds buffer
            return accessToken;
        }
        
        throw new RuntimeException("Failed to get PayPal access token");
    }}