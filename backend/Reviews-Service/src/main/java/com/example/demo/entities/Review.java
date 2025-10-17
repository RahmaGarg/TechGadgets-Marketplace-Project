package com.example.demo.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "product_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long clientId;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Long orderId; // Pour vérifier que le client a acheté le produit
    
    @Column(nullable = false)
    private Integer rating; // Note de 1 à 5
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @Column(length = 500)
    private String imagePath; // Chemin vers l'image uploadée
    
    @Column(nullable = false)
    private Boolean verified = false; // Indique si l'achat est vérifié
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    
    @Column(nullable = false)
    private Boolean active = true; // Pour soft delete
}