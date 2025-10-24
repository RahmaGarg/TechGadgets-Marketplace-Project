package com.example.demo.events;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreatedEvent {
    private Long reviewId;
    private Long productId;
    private Long sellerId;
    private Long clientId;
    private Integer rating;
}