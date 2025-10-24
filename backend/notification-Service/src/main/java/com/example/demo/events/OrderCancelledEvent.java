package com.example.demo.events;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelledEvent {
    private Long orderId;
    private Long clientId;
    private Long sellerId;
    private String cancellationReason;
}