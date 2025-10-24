package com.example.demo.events;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderShippedEvent {
    private Long orderId;
    private Long clientId;
    private String trackingNumber;
    private LocalDateTime estimatedDelivery;
}