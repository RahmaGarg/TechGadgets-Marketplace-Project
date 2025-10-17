package com.example.demo.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Review;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Vérifier si un client a déjà laissé un avis pour un produit
    boolean existsByClientIdAndProductIdAndActiveTrue(Long clientId, Long productId);
    
    // Récupérer l'avis d'un client pour un produit spécifique
    Optional<Review> findByClientIdAndProductIdAndActiveTrue(Long clientId, Long productId);
    
    // Récupérer tous les avis d'un produit (actifs seulement)
    Page<Review> findByProductIdAndActiveTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    // Récupérer tous les avis d'un client
    Page<Review> findByClientIdAndActiveTrueOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    
    // Compter les avis d'un produit
    long countByProductIdAndActiveTrue(Long productId);
    
    // Calculer la moyenne des notes pour un produit
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.active = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // Compter les avis par note pour un produit
    long countByProductIdAndRatingAndActiveTrue(Long productId, Integer rating);
    
    // Récupérer les derniers avis (tous produits)
    Page<Review> findByActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Récupérer les avis vérifiés d'un produit
    Page<Review> findByProductIdAndVerifiedTrueAndActiveTrueOrderByCreatedAtDesc(
        Long productId, Pageable pageable);
    
    // Vérifier si un client a acheté un produit via une commande
    boolean existsByClientIdAndProductIdAndOrderIdAndActiveTrue(
        Long clientId, Long productId, Long orderId);
}