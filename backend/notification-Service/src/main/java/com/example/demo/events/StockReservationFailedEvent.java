package com.example.demo.events;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockReservationFailedEvent {
    private Long orderId;
    private Long clientId;
    private Long productId;
    private String productName;
}