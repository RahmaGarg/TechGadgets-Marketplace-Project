package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Stock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productId = :productId")
    Optional<Stock> findByProductIdWithLock(Long productId);
    
    Optional<Stock> findByProductId(Long productId);
    
    @Query("SELECT s FROM Stock s WHERE s.availableQuantity <= s.lowStockThreshold")
    List<Stock> findLowStockProducts();
    
    boolean existsByProductId(Long productId);
}