package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.CreateProductDTO;
import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.UpdateProductDTO;
import com.example.demo.enums.ProductStatus;
import com.example.demo.services.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@Slf4j
@Validated
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
        @Valid @RequestBody CreateProductDTO dto,
        @RequestHeader("userId") Long sellerId) {
        log.info("Création d'un produit par le vendeur: {}", sellerId);
        ProductDTO product = productService.createProduct(dto, sellerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        log.info("Récupération du produit avec ID: {}", id);
        ProductDTO product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ProductStatus status) {
        log.info("Récupération de tous les produits - page: {}, size: {}, status: {}", page, size, status);
        List<ProductDTO> products = productService.getAllProducts(page, size, status);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductDTO>> getProductsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Récupération des produits du vendeur: {}", sellerId);
        List<ProductDTO> products = productService.getProductsBySeller(sellerId, page, size);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDTO dto,
            @RequestHeader("userId") Long sellerId) {
        log.info("Mise à jour du produit {} par le vendeur: {}", id, sellerId);
        ProductDTO product = productService.updateProduct(id, dto, sellerId);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("userId") Long sellerId) {
        log.info("Suppression du produit {} par le vendeur: {}", id, sellerId);
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.noContent().build();
    }
    
}