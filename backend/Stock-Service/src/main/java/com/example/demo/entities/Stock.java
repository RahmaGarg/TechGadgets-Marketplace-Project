package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private Long productId;
    
    @Column(nullable = false)
    private Integer availableQuantity;
    
    @Column(nullable = false)
    private Integer reservedQuantity;
    
    @Column(nullable = false)
    private Integer totalQuantity;
    
    @Column(nullable = false)
    private Integer lowStockThreshold;
    
    @Version
    private Long version; // Pour gérer la concurrence optimiste
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (reservedQuantity == null) reservedQuantity = 0;
        if (totalQuantity == null) totalQuantity = availableQuantity;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Méthodes métier
    public boolean isAvailable(Integer quantity) {
        return availableQuantity >= quantity;
    }
    
    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }
    
    public void reserve(Integer quantity) {
        if (!isAvailable(quantity)) {
            throw new IllegalStateException("Stock insuffisant");
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
    }
    
    public void confirmSale(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Quantité réservée insuffisante");
        }
        reservedQuantity -= quantity;
        totalQuantity -= quantity;
    }
    
    public void release(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new IllegalStateException("Quantité réservée insuffisante");
        }
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }
    
    public void addStock(Integer quantity) {
        availableQuantity += quantity;
        totalQuantity += quantity;
    }
}