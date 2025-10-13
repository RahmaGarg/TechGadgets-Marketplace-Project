package com.example.demo.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.CategoryDTO;
import com.example.demo.dtos.CategoryRequestDTO;
import com.example.demo.entities.Category;
import com.example.demo.repositories.CategoryRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public CategoryDTO createCategory(CategoryRequestDTO dto) {
        log.info("Création d'une nouvelle catégorie: {}", dto.getName());
        
        // Vérifier si une catégorie avec ce nom existe déjà
        if (categoryRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }
        
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        
        Category saved = categoryRepository.save(category);
        log.info("Catégorie créée avec ID: {}", saved.getId());
        return convertToDTO(saved);
    }
    
    public CategoryDTO getCategory(Long id) {
        log.info("Récupération de la catégorie avec ID: {}", id);
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + id));
        return convertToDTO(category);
    }
    
    public List<CategoryDTO> getAllCategories(int page, int size) {
        log.info("Récupération de toutes les catégories avec pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Category> categories = categoryRepository.findAll(pageable);
        
        return categories.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDTO> getAllCategoriesNoPagination() {
        log.info("Récupération de toutes les catégories sans pagination");
        List<Category> categories = categoryRepository.findAll(Sort.by("name").ascending());
        
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public CategoryDTO updateCategory(Long id, CategoryRequestDTO dto) {
        log.info("Mise à jour de la catégorie {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + id));
        
        // Vérifier si le nouveau nom existe déjà (sauf si c'est le même)
        if (!dto.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new RuntimeException("Une catégorie avec ce nom existe déjà");
            }
        }
        
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        
        Category updated = categoryRepository.save(category);
        log.info("Catégorie {} mise à jour avec succès", id);
        return convertToDTO(updated);
    }
    
    public void deleteCategory(Long id) {
        log.info("Suppression de la catégorie {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec ID: " + id));
        
        // Vérifier s'il y a des produits associés
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer une catégorie contenant des produits");
        }
        
        categoryRepository.delete(category);
        log.info("Catégorie {} supprimée avec succès", id);
    }
    
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        
        // Compter le nombre de produits de manière optimisée (query COUNT au lieu de charger tous les produits)
        Long count = categoryRepository.countProductsByCategoryId(category.getId());
        dto.setProductCount(count != null ? count.intValue() : 0);
        
        return dto;
    }
}