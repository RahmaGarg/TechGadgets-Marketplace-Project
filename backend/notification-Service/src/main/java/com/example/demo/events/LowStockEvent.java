package com.example.demo.events;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LowStockEvent {
    private Long productId;
    private String productName;
    private Long sellerId;
    private Integer currentStock;
    private Integer threshold;
}



