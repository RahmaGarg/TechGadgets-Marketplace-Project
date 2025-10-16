package com.example.demo.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import com.example.demo.enums.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull
    private Double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String description;

    @NotBlank
    @Email
    private String customerEmail;

    @NotNull
    private PaymentMethod method;

    private String customerName;
    
    private String successUrl;
    
    private String failUrl;

    private Map<String, Object> metadata;
}