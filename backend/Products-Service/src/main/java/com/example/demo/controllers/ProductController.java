package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dtos.CreateProductDTO;
import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.UpdateProductDTO;
import com.example.demo.enums.ProductStatus;
import com.example.demo.services.ProductService;
import com.example.demo.services.FileStorageService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@Slf4j
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductDTO> createProduct(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") String price,
            @RequestParam("stock") String stock,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader("userId") Long sellerId) {
        log.info("Création d'un produit par le vendeur: {}", sellerId);
        
        CreateProductDTO dto = new CreateProductDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setPrice(new java.math.BigDecimal(price));
        dto.setStock(Integer.parseInt(stock));
        dto.setCategoryId(categoryId);
        
        // Store image if provided
        if (image != null && !image.isEmpty()) {
            String fileName = fileStorageService.storeFile(image);
            dto.setImageUrl(fileName);
        }
        
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
            @Valid @ModelAttribute UpdateProductDTO dto,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader("userId") Long sellerId) {
        log.info("Mise à jour du produit {} par le vendeur: {}", id, sellerId);
        
        // Store new image if provided
        if (image != null && !image.isEmpty()) {
            // Get old product to delete old image
            ProductDTO oldProduct = productService.getProduct(id);
            if (oldProduct.getImageUrl() != null) {
                fileStorageService.deleteFile(oldProduct.getImageUrl());
            }
            
            String fileName = fileStorageService.storeFile(image);
            dto.setImageUrl(fileName);
        }
        
        ProductDTO product = productService.updateProduct(id, dto, sellerId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("userId") Long sellerId) {
        log.info("Suppression du produit {} par le vendeur: {}", id, sellerId);
        
        // Get product to delete associated image
        ProductDTO product = productService.getProduct(id);
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }
        
        productService.deleteProduct(id, sellerId);
        return ResponseEntity.noContent().build();
    }
    // ========== ADMIN MODERATION ENDPOINTS ==========
    
    /**
     * Approve a product (ADMIN only)
     */
    @PostMapping("/{id}/approve")
    // @PreAuthorize("hasRole('ADMIN')") // Uncomment if you have security
    public ResponseEntity<ProductDTO> approveProduct(@PathVariable Long id) {
        log.info("Admin approving product: {}", id);
        ProductDTO approved = productService.approveProduct(id);
        return ResponseEntity.ok(approved);
    }
    
    /**
     * Reject a product with reason (ADMIN only)
     */

    
    /**
     * Get all pending products (ADMIN only)
     */
    @GetMapping("/pending")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductDTO>> getPendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching pending products for moderation");
        List<ProductDTO> pending = productService.getAllProducts(page, size, ProductStatus.PENDING);
        return ResponseEntity.ok(pending);
    }
    

    
}