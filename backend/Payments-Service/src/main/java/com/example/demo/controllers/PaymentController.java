package com.example.demo.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dtos.PaymentRequest;
import com.example.demo.dtos.PaymentResponse;
import com.example.demo.entities.Payment;
import com.example.demo.services.PaymentService;

@RestController
@RequestMapping("/payment")
@Validated
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {
        log.info("Initiating payment for method: {}", request.getMethod());
        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Payment> getPaymentStatus(@PathVariable String paymentId) {
        Payment payment = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(payment);
    }

    // PayPal callbacks
    @GetMapping("/paypal/success")
    public ResponseEntity<String> paypalSuccess(
            @RequestParam String token,
            @RequestParam String PayerID) {
        try {
            Payment payment = paymentService.completePayPalPayment(token);
            return ResponseEntity.ok("Payment completed successfully! Payment ID: " + payment.getPaymentId());
        } catch (Exception e) {
            log.error("PayPal payment completion failed", e);
            return ResponseEntity.badRequest().body("Payment failed: " + e.getMessage());
        }
    }

    @GetMapping("/paypal/cancel")
    public ResponseEntity<String> paypalCancel() {
        return ResponseEntity.ok("Payment was cancelled");
    }

    // Konnect callbacks
    @GetMapping("/konnect/success")
    public ResponseEntity<String> konnectSuccess(@RequestParam("payment_ref") String paymentRef) {
        try {
            Payment payment = paymentService.completeKonnectPayment(paymentRef);
            return ResponseEntity.ok("Payment completed successfully! Payment ID: " + payment.getPaymentId());
        } catch (Exception e) {
            log.error("Konnect payment verification failed", e);
            return ResponseEntity.badRequest().body("Payment verification failed: " + e.getMessage());
        }
    }

    @GetMapping("/konnect/fail")
    public ResponseEntity<String> konnectFail(@RequestParam(value = "payment_ref", required = false) String paymentRef) {
        log.info("Konnect payment failed or cancelled: {}", paymentRef);
        return ResponseEntity.ok("Payment was cancelled or failed");
    }
}