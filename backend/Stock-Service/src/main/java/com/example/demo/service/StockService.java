package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.AvailabilityResponse;
import com.example.demo.dtos.CheckAvailabilityRequest;
import com.example.demo.dtos.StockOperationRequest;
import com.example.demo.dtos.StockRequest;
import com.example.demo.dtos.StockResponse;
import com.example.demo.entities.Stock;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.StockException;
import com.example.demo.repositories.StockRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {
    
    private final StockRepository stockRepository;
    
    // Créer un nouveau stock pour un produit
    @Transactional
    public StockResponse createStock(StockRequest request) {
        if (stockRepository.existsByProductId(request.getProductId())) {
            throw new StockException("Stock existe déjà pour ce produit");
        }
        
        Stock stock = Stock.builder()
                .productId(request.getProductId())
                .availableQuantity(request.getQuantity())
                .reservedQuantity(0)
                .totalQuantity(request.getQuantity())
                .lowStockThreshold(request.getLowStockThreshold())
                .build();
        
        Stock saved = stockRepository.save(stock);
        log.info("Stock créé pour produit {}: {} unités", request.getProductId(), request.getQuantity());
        
        return mapToResponse(saved);
    }
    
    // Récupérer le stock d'un produit
    @Transactional(readOnly = true)
    public StockResponse getStockByProductId(Long productId) {
        Stock stock = findStockByProductId(productId);
        return mapToResponse(stock);
    }
    
    // Vérifier la disponibilité
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(CheckAvailabilityRequest request) {
        Stock stock = findStockByProductId(request.getProductId());
        
        boolean available = stock.isAvailable(request.getQuantity());
        String message = available 
            ? "Stock disponible" 
            : String.format("Stock insuffisant. Disponible: %d, Demandé: %d", 
                stock.getAvailableQuantity(), request.getQuantity());
        
        return AvailabilityResponse.builder()
                .productId(request.getProductId())
                .available(available)
                .availableQuantity(stock.getAvailableQuantity())
                .requestedQuantity(request.getQuantity())
                .message(message)
                .build();
    }
    
    // Réserver du stock (lors de la création d'une commande)
    @Transactional
    public StockResponse reserveStock(StockOperationRequest request) {
        Stock stock = findStockByProductIdWithLock(request.getProductId());
        
        if (!stock.isAvailable(request.getQuantity())) {
            throw new StockException(
                String.format("Stock insuffisant pour produit %d. Disponible: %d, Demandé: %d",
                    request.getProductId(), stock.getAvailableQuantity(), request.getQuantity())
            );
        }
        
        stock.reserve(request.getQuantity());
        Stock updated = stockRepository.save(stock);
        
        log.info("Stock réservé pour produit {}: {} unités", request.getProductId(), request.getQuantity());
        
        // TODO: Envoyer notification si stock bas
        if (updated.isLowStock()) {
            log.warn("⚠️ ALERTE: Stock bas pour produit {}. Quantité: {}", 
                updated.getProductId(), updated.getAvailableQuantity());
            // Ici, tu appelleras ton notification-service
        }
        
        return mapToResponse(updated);
    }
    
    // Confirmer la vente (après paiement réussi)
    @Transactional
    public StockResponse confirmSale(StockOperationRequest request) {
        Stock stock = findStockByProductIdWithLock(request.getProductId());
        
        stock.confirmSale(request.getQuantity());
        Stock updated = stockRepository.save(stock);
        
        log.info("Vente confirmée pour produit {}: {} unités", request.getProductId(), request.getQuantity());
        return mapToResponse(updated);
    }
    
    // Libérer le stock (en cas d'annulation de commande)
    @Transactional
    public StockResponse releaseStock(StockOperationRequest request) {
        Stock stock = findStockByProductIdWithLock(request.getProductId());
        
        stock.release(request.getQuantity());
        Stock updated = stockRepository.save(stock);
        
        log.info("Stock libéré pour produit {}: {} unités", request.getProductId(), request.getQuantity());
        return mapToResponse(updated);
    }
    
    // Ajouter du stock (réapprovisionnement)
    @Transactional
    public StockResponse addStock(StockOperationRequest request) {
        Stock stock = findStockByProductIdWithLock(request.getProductId());
        
        stock.addStock(request.getQuantity());
        Stock updated = stockRepository.save(stock);
        
        log.info("Stock ajouté pour produit {}: {} unités", request.getProductId(), request.getQuantity());
        return mapToResponse(updated);
    }
    
    // Récupérer tous les produits avec stock bas
    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockProducts() {
        return stockRepository.findLowStockProducts().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Méthodes utilitaires
    private Stock findStockByProductId(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Stock non trouvé pour produit: " + productId));
    }
    
    private Stock findStockByProductIdWithLock(Long productId) {
        return stockRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Stock non trouvé pour produit: " + productId));
    }
    
    private StockResponse mapToResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .productId(stock.getProductId())
                .availableQuantity(stock.getAvailableQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .totalQuantity(stock.getTotalQuantity())
                .lowStockThreshold(stock.getLowStockThreshold())
                .isLowStock(stock.isLowStock())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}