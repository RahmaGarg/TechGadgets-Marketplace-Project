package com.example.demo.events;
import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private Long clientId;
    private Long sellerId;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
}