package com.example.demo.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Vérifier si une catégorie avec ce nom existe déjà
    boolean existsByName(String name);
    
    // Rechercher une catégorie par son nom (optionnel, utile pour les recherches)
    Category findByName(String name);
    
    // Compter les produits d'une catégorie de manière optimisée (sans charger tous les produits)
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}