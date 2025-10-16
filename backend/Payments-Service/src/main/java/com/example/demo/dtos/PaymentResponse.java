package com.example.demo.dtos;
import java.time.Instant;

import com.example.demo.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String paymentId;
    private PaymentStatus status;
    private String approvalUrl;
    private String externalPaymentId;
    private String gateway;
    private Instant expiresAt;
    private String message;
}
