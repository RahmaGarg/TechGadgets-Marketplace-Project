package com.example.demo.events;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRejectedEvent {
    private Long productId;
    private String productName;
    private Long sellerId;
    private String rejectionReason;
}