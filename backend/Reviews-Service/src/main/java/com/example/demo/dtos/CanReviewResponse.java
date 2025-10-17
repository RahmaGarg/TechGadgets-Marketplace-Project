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
public class CanReviewResponse {
    private Boolean canReview;
    private String reason;
    private Boolean hasExistingReview;
    private Boolean hasPurchased;
}