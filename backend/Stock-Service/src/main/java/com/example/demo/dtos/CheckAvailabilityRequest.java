package com.example.demo.dtos;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckAvailabilityRequest {
    @NotNull(message = "Product ID est obligatoire")
    private Long productId;
    
    @NotNull(message = "Quantité est obligatoire")
    @Min(value = 1, message = "Quantité doit être >= 1")
    private Integer quantity;
}