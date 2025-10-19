package com.example.demo.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dtos.ApiResponse;
import com.example.demo.dtos.AvailabilityResponse;
import com.example.demo.dtos.CheckAvailabilityRequest;
import com.example.demo.dtos.StockOperationRequest;
import com.example.demo.dtos.StockRequest;
import com.example.demo.dtos.StockResponse;
import com.example.demo.service.StockService;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {
    
    private final StockService stockService;
    
    // Créer un nouveau stock
    @PostMapping
    public ResponseEntity<ApiResponse<StockResponse>> createStock(
            @Valid @RequestBody StockRequest request) {
        StockResponse response = stockService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock créé avec succès", response));
    }
    
    // Récupérer le stock d'un produit
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<StockResponse>> getStockByProductId(
            @PathVariable Long productId) {
        StockResponse response = stockService.getStockByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Stock récupéré", response));
    }
    
    // Vérifier la disponibilité
    @PostMapping("/check-availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @Valid @RequestBody CheckAvailabilityRequest request) {
        AvailabilityResponse response = stockService.checkAvailability(request);
        return ResponseEntity.ok(ApiResponse.success("Vérification effectuée", response));
    }
    
    // Réserver du stock
    @PutMapping("/product/{productId}/reserve")
    public ResponseEntity<ApiResponse<StockResponse>> reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockOperationRequest request) {
        request.setProductId(productId);
        StockResponse response = stockService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock réservé avec succès", response));
    }
    
    // Confirmer la vente
    @PutMapping("/product/{productId}/confirm")
    public ResponseEntity<ApiResponse<StockResponse>> confirmSale(
            @PathVariable Long productId,
            @Valid @RequestBody StockOperationRequest request) {
        request.setProductId(productId);
        StockResponse response = stockService.confirmSale(request);
        return ResponseEntity.ok(ApiResponse.success("Vente confirmée avec succès", response));
    }
    
    // Libérer le stock (annulation)
    @PutMapping("/product/{productId}/release")
    public ResponseEntity<ApiResponse<StockResponse>> releaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockOperationRequest request) {
        request.setProductId(productId);
        StockResponse response = stockService.releaseStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock libéré avec succès", response));
    }
    
    // Ajouter du stock (réapprovisionnement)
    @PutMapping("/product/{productId}/add")
    public ResponseEntity<ApiResponse<StockResponse>> addStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockOperationRequest request) {
        request.setProductId(productId);
        StockResponse response = stockService.addStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock ajouté avec succès", response));
    }
    
    // Récupérer les produits avec stock bas
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<StockResponse>>> getLowStockProducts() {
        List<StockResponse> response = stockService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(
            "Produits avec stock bas récupérés", response));
    }
}