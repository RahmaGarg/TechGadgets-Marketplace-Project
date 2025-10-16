package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.PaymentRequest;
import com.example.demo.dtos.PaymentResponse;
import com.example.demo.entities.Payment;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.PaymentRepository;

@Service
@Slf4j
public class PaymentService {
    
    private final PayPalService payPalService;
    private final KonnectService konnectService;
    private final PaymentRepository paymentRepository;
    
    public PaymentService(PayPalService payPalService, 
                         KonnectService konnectService,
                         PaymentRepository paymentRepository) {
        this.payPalService = payPalService;
        this.konnectService = konnectService;
        this.paymentRepository = paymentRepository;
    }
    
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Initiating payment with method: {}", request.getMethod());
        
        if (request.getMethod() == PaymentMethod.PAYPAL) {
            return payPalService.createPayment(request);
        } else if (request.getMethod() == PaymentMethod.KONNECT) {
            return konnectService.createPayment(request);
        }
        
        throw new IllegalArgumentException("Unsupported payment method: " + request.getMethod());
    }
    
    public Payment getPaymentStatus(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }
    
    public Payment completePayPalPayment(String orderId) {
        return payPalService.capturePayment(orderId);
    }
    
    public Payment completeKonnectPayment(String paymentRef) {
        return konnectService.verifyPayment(paymentRef);
    }
    
    public Payment updatePaymentStatus(String externalPaymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }
}