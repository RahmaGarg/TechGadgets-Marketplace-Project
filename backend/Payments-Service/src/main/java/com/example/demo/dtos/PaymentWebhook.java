package com.example.demo.dtos;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhook {
    private String eventType;
    private Map<String, Object> data;
    private String signature;
}