package com.example.demo.dtos;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDTO {
    @NotBlank(message = "Le nom du produit est requis")
    private String name;
    
    private String description;
    
    @NotNull(message = "Le prix est requis")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal price;
    
    @NotNull(message = "Le stock est requis")
    @PositiveOrZero(message = "Le stock ne peut pas être négatif")
    private Integer stock;
    
    private Long categoryId;
    private String imageUrl;
}
