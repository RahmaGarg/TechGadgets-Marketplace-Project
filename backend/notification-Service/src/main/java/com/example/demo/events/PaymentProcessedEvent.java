package com.example.demo.events;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessedEvent {
    private Long paymentId;
    private Long orderId;
    private Long clientId;
    private BigDecimal amount;
    private String paymentMethod;
}
