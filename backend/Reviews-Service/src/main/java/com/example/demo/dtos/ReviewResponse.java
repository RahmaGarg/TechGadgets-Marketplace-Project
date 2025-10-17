package com.example.demo.dtos;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long clientId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String comment;
    private String imageUrl;
    private Boolean verified;
    private LocalDateTime createdAt;
}
