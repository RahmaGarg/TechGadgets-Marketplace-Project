package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Product;
import com.example.demo.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findBySellerId(Long sellerId, Pageable pageable);
    
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    
    List<Product> findByCategoryId(Long categoryId);
    
    List<Product> findByStatus(ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' AND p.stock > 0")
    Page<Product> findAvailableProducts(Pageable pageable);
}