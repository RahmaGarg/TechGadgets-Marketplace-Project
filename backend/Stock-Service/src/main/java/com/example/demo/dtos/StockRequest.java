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
@Builder
public class StockRequest {
    @NotNull(message = "Product ID est obligatoire")
    private Long productId;
    
    @NotNull(message = "Quantité est obligatoire")
    @Min(value = 0, message = "Quantité doit être >= 0")
    private Integer quantity;
    
    @Min(value = 1, message = "Seuil doit être >= 1")
    private Integer lowStockThreshold = 10;
}