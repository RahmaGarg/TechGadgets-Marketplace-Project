package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.CategoryDTO;
import com.example.demo.dtos.CategoryRequestDTO;
import com.example.demo.services.CategoryService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/categories")
@Slf4j
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * ADMIN ONLY - Créer une nouvelle catégorie
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO dto) {
        log.info("Création d'une catégorie: {}", dto.getName());
        CategoryDTO category = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * PUBLIC - Récupérer une catégorie par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long id) {
        log.info("Récupération de la catégorie avec ID: {}", id);
        CategoryDTO category = categoryService.getCategory(id);
        return ResponseEntity.ok(category);
    }

    /**
     * PUBLIC - Récupérer toutes les catégories avec pagination
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Récupération de toutes les catégories - page: {}, size: {}", page, size);
        List<CategoryDTO> categories = categoryService.getAllCategories(page, size);
        return ResponseEntity.ok(categories);
    }

    /**
     * PUBLIC - Récupérer toutes les catégories sans pagination (utile pour les dropdowns)
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesNoPagination() {
        log.info("Récupération de toutes les catégories sans pagination");
        List<CategoryDTO> categories = categoryService.getAllCategoriesNoPagination();
        return ResponseEntity.ok(categories);
    }

    /**
     * ADMIN ONLY - Mettre à jour une catégorie
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        log.info("Mise à jour de la catégorie {}", id);
        CategoryDTO category = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(category);
    }

    /**
     * ADMIN ONLY - Supprimer une catégorie
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Suppression de la catégorie {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}