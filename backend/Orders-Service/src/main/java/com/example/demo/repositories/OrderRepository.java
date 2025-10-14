package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Order;
import com.example.demo.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Méthodes basiques
    List<Order> findByClientId(Long clientId);
    List<Order> findBySellerId(Long sellerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);
    List<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status);
    
    // Méthodes avec pagination
    Page<Order> findByClientId(Long clientId, Pageable pageable);
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByClientIdAndStatus(Long clientId, OrderStatus status, Pageable pageable);
    Page<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);
    
    // Recherche par date
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}