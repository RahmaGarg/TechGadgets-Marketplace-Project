package com.example.demo.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.CreateProductDTO;
import com.example.demo.dtos.ProductDTO;
import com.example.demo.dtos.UpdateProductDTO;
import com.example.demo.entities.Category;
import com.example.demo.entities.Product;
import com.example.demo.enums.ProductStatus;
import com.example.demo.events.ProductApprovedEvent;
import com.example.demo.events.ProductCreatedEvent;
import com.example.demo.events.ProductDeletedEvent;
import com.example.demo.events.ProductRejectedEvent;
import com.example.demo.repositories.CategoryRepository;
import com.example.demo.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private KafkaProducerService kafkaProducerService;
    
    public ProductDTO createProduct(CreateProductDTO dto, Long sellerId) {
        log.info("Création d'un nouveau produit pour le vendeur: {}", sellerId);
        
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSellerId(sellerId);
        product.setImageUrl(dto.getImageUrl());
        product.setStatus(ProductStatus.PENDING); // Initial status
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }
        
        Product saved = productRepository.save(product);
        log.info("Produit créé avec ID: {}", saved.getId());
        
        // 🔥 SEND KAFKA EVENT
        ProductCreatedEvent event = ProductCreatedEvent.builder()
                .productId(saved.getId())
                .productName(saved.getName())
                .sellerId(saved.getSellerId())
                .build();
        kafkaProducerService.sendProductCreatedEvent(event);
        
        return convertToDTO(saved);
    }
    
    public ProductDTO getProduct(Long id) {
        log.info("Récupération du produit avec ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        return convertToDTO(product);
    }
    
    public List<ProductDTO> getAllProducts(int page, int size, ProductStatus status) {
        log.info("Récupération de tous les produits avec pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Product> products;
        if (status != null) {
            products = productRepository.findByStatus(status, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }
        
        return products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ProductDTO> getProductsBySeller(Long sellerId, int page, int size) {
        log.info("Récupération des produits pour le vendeur: {}", sellerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findBySellerId(sellerId, pageable);
        
        return products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ProductDTO updateProduct(Long id, UpdateProductDTO dto, Long sellerId) {
        log.info("Mise à jour du produit {} par le vendeur {}", id, sellerId);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        // Vérifier que le vendeur est bien le propriétaire du produit
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Non autorisé à modifier ce produit");
        }
        
        // Mettre à jour les champs
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }
        if (dto.getImageUrl() != null) {
            product.setImageUrl(dto.getImageUrl());
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + dto.getCategoryId()));
            product.setCategory(category);
        }
        
        Product updated = productRepository.save(product);
        log.info("Produit {} mis à jour avec succès", id);
        return convertToDTO(updated);
    }
    
    public void deleteProduct(Long id, Long sellerId) {
        log.info("Suppression du produit {} par le vendeur {}", id, sellerId);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        // Vérifier que le vendeur est bien le propriétaire du produit
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("Non autorisé à supprimer ce produit");
        }
        
        // Store product info before deletion
        Long productId = product.getId();
        String productName = product.getName();
        Long productSellerId = product.getSellerId();
        
        productRepository.delete(product);
        log.info("Produit {} supprimé avec succès", id);
        
        // 🔥 SEND KAFKA EVENT for stock-service
        ProductDeletedEvent event = ProductDeletedEvent.builder()
                .productId(productId)
                .productName(productName)
                .sellerId(productSellerId)
                .build();
        kafkaProducerService.sendProductDeletedEvent(event);
    }
    
    public ProductDTO updateProductStatus(Long id, ProductStatus status) {
        log.info("Mise à jour du statut du produit {} vers {}", id, status);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        ProductStatus oldStatus = product.getStatus();
        product.setStatus(status);
        Product updated = productRepository.save(product);
        
        log.info("Statut du produit {} mis à jour vers {}", id, status);
        
        // 🔥 SEND KAFKA EVENTS based on status change
        if (status == ProductStatus.APPROVED && oldStatus != ProductStatus.APPROVED) {
            ProductApprovedEvent event = ProductApprovedEvent.builder()
                    .productId(updated.getId())
                    .productName(updated.getName())
                    .sellerId(updated.getSellerId())
                    .build();
            kafkaProducerService.sendProductApprovedEvent(event);
        } else if (status == ProductStatus.REJECTED && oldStatus != ProductStatus.REJECTED) {
            ProductRejectedEvent event = ProductRejectedEvent.builder()
                    .productId(updated.getId())
                    .productName(updated.getName())
                    .sellerId(updated.getSellerId())
                    .rejectionReason("Produit non conforme aux standards") // You can add this as parameter
                    .build();
            kafkaProducerService.sendProductRejectedEvent(event);
        }
        
        return convertToDTO(updated);
    }
    
    // 🆕 NEW METHOD: Approve product with reason
    public ProductDTO approveProduct(Long id) {
        log.info("Approbation du produit {}", id);
        return updateProductStatus(id, ProductStatus.APPROVED);
    }
    
    // 🆕 NEW METHOD: Reject product with reason
    public ProductDTO rejectProduct(Long id, String rejectionReason) {
        log.info("Rejet du produit {} avec raison: {}", id, rejectionReason);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec ID: " + id));
        
        product.setStatus(ProductStatus.REJECTED);
        Product updated = productRepository.save(product);
        
        // 🔥 SEND KAFKA EVENT with custom reason
        ProductRejectedEvent event = ProductRejectedEvent.builder()
                .productId(updated.getId())
                .productName(updated.getName())
                .sellerId(updated.getSellerId())
                .rejectionReason(rejectionReason)
                .build();
        kafkaProducerService.sendProductRejectedEvent(event);
        
        log.info("Produit {} rejeté avec succès", id);
        return convertToDTO(updated);
    }
    
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setSellerId(product.getSellerId());
        dto.setImageUrl(product.getImageUrl());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }
        
        return dto;
    }
}